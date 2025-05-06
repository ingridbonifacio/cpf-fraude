package cpfvalidador;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Versao3Threads {
    private static final String ENTRADA_DIR = "cpfs";
    private static int validos = 0;
    private static int invalidos = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) throws Exception {{
        long inicio = System.currentTimeMillis();

        File pasta = new File(ENTRADA_DIR);
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".txt"));
        if (arquivos == null || arquivos.length != 30) {{
            System.out.println("aguardando 30 arquivos na pasta 'entradas'.");
            return;
        }}

        Arrays.sort(arquivos, Comparator.comparing(File::getName));
        ExecutorService executor = Executors.newFixedThreadPool(3);

        int arquivosPorThread = (int) Math.ceil(30.0 / 3);

        for (int i = 0; i < 3; i++) {{
            int inicioIdx = i * arquivosPorThread;
            int fimIdx = Math.min(inicioIdx + arquivosPorThread, arquivos.length);
            File[] lote = Arrays.copyOfRange(arquivos, inicioIdx, fimIdx);

            executor.execute(() -> {{
                int v = 0, inv = 0;
                for (File arq : lote) {{
                    try {{
                        List<String> linhas = Files.readAllLines(arq.toPath());
                        for (String cpf : linhas) {{
                            if (CPFValidator.validaCPF(cpf)) v++;
                            else inv++;
                        }}
                    }} catch (IOException e) {{
                        e.printStackTrace();
                    }}
                }}
                synchronized (lock) {{
                    validos += v;
                    invalidos += inv;
                }}
            }});
        }}

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long fim = System.currentTimeMillis();
        long tempo = fim - inicio;

        System.out.println("Versão 3 threads:");
        System.out.println("CPFs válidos: " + validos);
        System.out.println("CPFs inválidos: " + invalidos);
        System.out.println("Tempo total: " + tempo + "ms");

        try (PrintWriter pw = new PrintWriter("versao_3_threads.txt")) {{
            pw.println("CPFs válidos: " + validos);
            pw.println("CPFs inválidos: " + invalidos);
            pw.println("Tempo total: " + tempo + "ms");
        }}
    }}
}