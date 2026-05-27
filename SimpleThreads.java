public class SimpleThreads {

    // Exibe uma mensagem, precedida pelo nome da thread atual
    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private static class MessageLoop
            implements Runnable {
        public void run() {
            String importantInfo[] = {
                "Tô sem criatividade 1",
                "Tô sem criatividade 2",
                "Tô sem criatividade 3",
                "Tô sem criatividade 4"
            };
            try {
                for (int i = 0; i < importantInfo.length; i++) {
                    // Pausa por 4 segundos
                    Thread.sleep(4000);
                    // Exibe uma mensagem
                    threadMessage(importantInfo[i]);
                }
            } catch (InterruptedException e) {
                threadMessage("Ainda não terminei!");
            }
        }
    }

    /**
     * Tarefa intensiva de CPU: encontra todos os números primos até um limite grande
     * usando divisão exaustiva. Verifica interrupção após cada candidato
     * para poder ser parada se ultrapassar o tempo disponível.
     */
    private static class PrimeCalculator
            implements Runnable {
        private final long limit;

        PrimeCalculator(long limit) {
            this.limit = limit;
        }

        public void run() {
            threadMessage("Iniciando busca de primos até " + limit);
            long count = 0;

            for (long n = 2; n <= limit; n++) {

                // Respeita pedidos de interrupção entre candidatos
                if (Thread.interrupted()) {
                    threadMessage("Busca de primos interrompida! Primos encontrados até agora: " + count);
                    return;
                }

                if (isPrime(n)) {
                    count++;
                }
            }

            threadMessage("Busca de primos finalizada. Total de primos encontrados: " + count);
        }

        private boolean isPrime(long n) {
            if (n < 2) return false;
            if (n == 2) return true;
            if (n % 2 == 0) return false;
            for (long i = 3; i * i <= n; i += 2) {
                if (n % i == 0) return false;
            }
            return true;
        }
    }

    public static void main(String args[])
            throws InterruptedException {

        // Atraso, em milissegundos, antes de interromper a thread MessageLoop (padrão uma hora)
        long patience = 1000 * 60 * 60;

        // Se houver argumento na linha de comando, define a paciência em segundos
        if (args.length > 0) {
            try {
                patience = Long.parseLong(args[0]) * 1000;
            } catch (NumberFormatException e) {
                System.err.println("O argumento deve ser um inteiro.");
                System.exit(1);
            }
        }

        // ── Thread MessageLoop (original) (traduzi, ninguém merece...)────────────────────────────────────
        threadMessage("Iniciando thread MessageLoop");
        long startTime = System.currentTimeMillis();
        Thread t = new Thread(new MessageLoop());
        t.start();

        threadMessage("Aguardando a thread MessageLoop terminar");
        while (t.isAlive()) {
            threadMessage("Ainda esperando...");
            t.join(1000);
            if (((System.currentTimeMillis() - startTime) > patience) && t.isAlive()) {
                threadMessage("Cansei de esperar!");
                t.interrupt();
                t.join();
            }
        }
        threadMessage("Finalmente!");

        // ── Thread PrimeCalculator (nova) ─────────────────────────────────────
        // Limite de tempo para a tarefa intensiva de CPU: 2 segundos
        long cpuTimeLimit = 2000;
        long primeLimit   = 2_000_000_000L; // busca primos até 2 bilhões

        threadMessage("Iniciando thread PrimeCalculator (limite: " + primeLimit + ")");
        long cpuStart = System.currentTimeMillis();
        Thread cpuThread = new Thread(new PrimeCalculator(primeLimit));
        cpuThread.start();

        // Monitora a thread de CPU e a interrompe se ultrapassar cpuTimeLimit
        while (cpuThread.isAlive()) {
            cpuThread.join(500); // verifica a cada 500 ms
            if ((System.currentTimeMillis() - cpuStart) > cpuTimeLimit && cpuThread.isAlive()) {
                threadMessage("Limite de tempo de CPU excedido! Interrompendo PrimeCalculator...");
                cpuThread.interrupt();
                cpuThread.join();
            }
        }
        threadMessage("Thread PrimeCalculator concluída.");
    }
}