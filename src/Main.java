import java.io.IOException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

public class Main {
    private static final String NOME_VIDEO_SAIDA = "agente_labirinto.avi";
    private static final int FPS = 5;
    private static final int TAMANHO_BLOCO = 40; 

    private static final Scalar COR_PAREDE = new Scalar(80, 80, 80);
    private static final Scalar COR_CORREDOR = new Scalar(255, 255, 255);
    private static final Scalar COR_AGENTE = new Scalar(255, 0, 0);
    private static final Scalar COR_COMIDA = new Scalar(0, 255, 255);
    private static final Scalar COR_ENTRADA = new Scalar(0, 255, 0);
    private static final Scalar COR_SAIDA = new Scalar(0, 0, 255);

    private static final String ARQUIVO_MAPA = "maze.txt";
    private static final int TOTAL_COMIDAS = 4;
    private static final int MAX_PASSOS = 500; 

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoWriter videoWriter = null;
        AmbienteLabirinto ambiente = null;

        try {
            ambiente = new AmbienteLabirinto(ARQUIVO_MAPA, TOTAL_COMIDAS);
            Agente agente = new Agente(ambiente);

            Size frameSize = new Size(
                ambiente.getColunas() * TAMANHO_BLOCO,
                ambiente.getLinhas() * TAMANHO_BLOCO
            );
            int fourcc = VideoWriter.fourcc('M', 'J', 'P', 'G');
            videoWriter = new VideoWriter(NOME_VIDEO_SAIDA, fourcc, FPS, frameSize, true);

            if (!videoWriter.isOpened()) {
                System.err.println("Erro ao abrir o gravador de vídeo.");
                return;
            }

            System.out.println("Iniciando simulação e gravação do vídeo...\n");

            Mat frameInicial = desenharFrame(ambiente);
            videoWriter.write(frameInicial);

            int passoAtual = 0;
            while (ambiente.isJogoAtivo() && passoAtual < MAX_PASSOS) {
                agente.atuar();

                System.out.println("--- Passo " + passoAtual + " ---");
                imprimirLabirinto(ambiente);

                Mat frameAtual = desenharFrame(ambiente);
                videoWriter.write(frameAtual);

                passoAtual++;

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("\nSimulação concluída. Finalizando vídeo...");

        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo do mapa: " + e.getMessage());
        } finally {
            if (videoWriter != null) {
                videoWriter.release();
            }
            System.out.println("Vídeo '" + NOME_VIDEO_SAIDA + "' gerado com sucesso!");
        }

        if (ambiente != null) {
            if (!ambiente.isJogoAtivo()) {
                System.out.println("Agente alcançou a Saída!");
            } else {
                System.out.println("Limite de passos atingido.");
            }

            int recompensaFinal = ambiente.calcularRecompensa();
            System.out.println("\nRECOMPENSA FINAL: " + recompensaFinal + " pts");
            System.out.println("Total de Comidas Coletadas: " + ambiente.getComidasColetadas());
            System.out.println("Total de Passos: " + ambiente.getPassos());
        }
    }

    public static Mat desenharFrame(AmbienteLabirinto ambiente) {
        int linhas = ambiente.getLinhas();
        int colunas = ambiente.getColunas();
        char[][] mapa = ambiente.getMapa();

        Mat imagem = Mat.zeros(linhas * TAMANHO_BLOCO, colunas * TAMANHO_BLOCO, 16); // CV_8UC3
        
        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                Scalar cor = COR_CORREDOR;
                if (r == ambiente.agenteR && c == ambiente.agenteC) {
                    cor = COR_AGENTE;
                } else {
                    char conteudo = mapa[r][c];
                    switch (conteudo) {
                        case 'X': cor = COR_PAREDE; break;
                        case 'O': cor = COR_COMIDA; break;
                        case 'E': cor = COR_ENTRADA; break;
                        case 'S': cor = COR_SAIDA; break;
                        case '_': cor = COR_CORREDOR; break;
                    }
                }

                Point p1 = new Point(c * TAMANHO_BLOCO, r * TAMANHO_BLOCO);
                Point p2 = new Point((c + 1) * TAMANHO_BLOCO, (r + 1) * TAMANHO_BLOCO);
                Imgproc.rectangle(imagem, p1, p2, cor, -1);
                Imgproc.rectangle(imagem, p1, p2, new Scalar(0, 0, 0), 1);
            }
        }
        return imagem;
    }

    public static void imprimirLabirinto(AmbienteLabirinto ambiente) {
        char[][] mapa = ambiente.getMapa();
        int linhas = ambiente.getLinhas();
        int colunas = ambiente.getColunas();

        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                if (r == ambiente.agenteR && c == ambiente.agenteC) {
                    System.out.print('A'); 
                } else {
                    System.out.print(mapa[r][c]);
                }
            }
            System.out.println();
        }
        System.out.println(); 
    }
}
