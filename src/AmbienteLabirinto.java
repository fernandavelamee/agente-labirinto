import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AmbienteLabirinto {
    private char[][] mapa;
    private int linhas;
    private int colunas;
    int agenteR;
    int agenteC;
    private char direcaoAgente; 
    private int totalComidasIniciais;

    private int comidasColetadas = 0;
    private int passos = 0;
    private boolean jogoAtivo = true;

    public AmbienteLabirinto(String arquivoMapa, int totalComidasIniciais) throws IOException {
        this.totalComidasIniciais = totalComidasIniciais;
        carregarMapa(arquivoMapa);
        encontrarPosInicial();
    }

    private void carregarMapa(String arquivoMapa) throws IOException {
        List<char[]> mapaList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(arquivoMapa))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                mapaList.add(linha.trim().toCharArray());
            }
        }
        this.linhas = mapaList.size();
        this.colunas = mapaList.get(0).length;
        this.mapa = new char[linhas][colunas];
        for (int i = 0; i < linhas; i++) {
            this.mapa[i] = mapaList.get(i);
        }
    }

    private void encontrarPosInicial() {
        this.agenteR = 1;
        this.agenteC = 1;
        if (mapa[agenteR][agenteC] == 'E') {
            mapa[agenteR][agenteC] = '_';
        }
        this.direcaoAgente = 'S';
    }

    public char[][] getSensor() {
        char[][] sensor = new char[3][3];
        for (int rSen = 0; rSen < 3; rSen++) {
            for (int cSen = 0; cSen < 3; cSen++) {
                if (rSen == 2 && cSen == 2) {
                    sensor[rSen][cSen] = direcaoAgente;
                    continue;
                }
                int mapaR = agenteR + rSen - 1;
                int mapaC = agenteC + cSen - 1;
                if (mapaR >= 0 && mapaR < linhas && mapaC >= 0 && mapaC < colunas) {
                    sensor[rSen][cSen] = mapa[mapaR][mapaC];
                } else {
                    sensor[rSen][cSen] = 'X';
                }
            }
        }
        return sensor;
    }

    public int[][] getFoodSensor() {
        int[][] foodCount = new int[3][3];
        for (int r = agenteR - 1; r >= 0; r--) {
            if (mapa[r][agenteC] == 'X') break;
            if (mapa[r][agenteC] == 'O') foodCount[0][1]++;
        }
        for (int r = agenteR + 1; r < linhas; r++) {
            if (mapa[r][agenteC] == 'X') break;
            if (mapa[r][agenteC] == 'O') foodCount[2][1]++;
        }
        for (int c = agenteC + 1; c < colunas; c++) {
            if (mapa[agenteR][c] == 'X') break;
            if (mapa[agenteR][c] == 'O') foodCount[1][2]++;
        }
        for (int c = agenteC - 1; c >= 0; c--) {
            if (mapa[agenteR][c] == 'X') break;
            if (mapa[agenteR][c] == 'O') foodCount[1][0]++;
        }
        return foodCount;
    }
    
   
    public int[][] getSensorDeVarredura() {
        int[][] contagemComida = new int[3][3]; 

        
        int[][] direcoes = {
            {-1, 0}, {1, 0}, {0, 1}, {0, -1}, 
            {-1, 1}, {-1, -1}, {1, 1}, {1, -1}  
        };
        
        int[][] posicoesResultado = {
            {0, 1}, {2, 1}, {1, 2}, {1, 0}, 
            {0, 2}, {0, 0}, {2, 2}, {2, 0}  
        };

        for (int i = 0; i < direcoes.length; i++) {
            int dLinha = direcoes[i][0];
            int dColuna = direcoes[i][1];
            
            int rAtual = agenteR + dLinha;
            int cAtual = agenteC + dColuna;
            int contador = 0;

            while (rAtual >= 0 && rAtual < linhas && cAtual >= 0 && cAtual < colunas) {
                if (mapa[rAtual][cAtual] == 'X') {
                    break;
                }
                if (mapa[rAtual][cAtual] == 'O') {
                    contador++;
                }
                
                rAtual += dLinha;
                cAtual += dColuna;
            }
            
            int resLinha = posicoesResultado[i][0];
            int resColuna = posicoesResultado[i][1];
            contagemComida[resLinha][resColuna] = contador;
        }

        return contagemComida;
    }


    public boolean setDirection(char direcao) {
        if (direcao == 'N' || direcao == 'S' || direcao == 'L' || direcao == 'O') {
            this.direcaoAgente = direcao;
            return true;
        }
        return false;
    }

    public boolean move() {
        if (!jogoAtivo) return false;
        int novaR = agenteR;
        int novaC = agenteC;
        if (direcaoAgente == 'N') novaR--;
        else if (direcaoAgente == 'S') novaR++;
        else if (direcaoAgente == 'L') novaC++;
        else if (direcaoAgente == 'O') novaC--;
        this.passos++;
        if (novaR >= 0 && novaR < linhas && novaC >= 0 && novaC < colunas && mapa[novaR][novaC] != 'X') {
            agenteR = novaR;
            agenteC = novaC;
            char conteudo = mapa[novaR][novaC];
            if (conteudo == 'O') {
                comidasColetadas++;
                mapa[novaR][novaC] = '_';
            } else if (conteudo == 'S') {
                if (comidasColetadas >= totalComidasIniciais) {
                    jogoAtivo = false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isJogoAtivo() { return jogoAtivo; }
    public char getDirecaoAgente() { return direcaoAgente; }
    public int getComidasColetadas() { return comidasColetadas; }
    public int getTotalComidasIniciais() { return totalComidasIniciais; }
    public int getPassos() { return passos; }
    public int calcularRecompensa() { return (comidasColetadas * 10) - passos; }
    public int getLinhas() { return this.linhas; }
    public int getColunas() { return this.colunas; }
    public char[][] getMapa() { return this.mapa; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                if (r == agenteR && c == agenteC) {
                    sb.append('A').append(' ');
                } else {
                    sb.append(mapa[r][c]).append(' ');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}