import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class Agente {
    private final AmbienteLabirinto ambiente; 
    private final Stack<String> pilhaCaminho; 
    private final Set<String> visitados; 

    public Agente(AmbienteLabirinto ambiente) {
        this.ambiente = ambiente;
        this.pilhaCaminho = new Stack<>();
        this.visitados = new HashSet<>();
    }

    private String getPosicaoString(int r, int c) {
        return r + "," + c;
    }
    
    private char getDirecaoPara(int r1, int c1, int r2, int c2) {
        if (r2 < r1) return 'N';
        if (r2 > r1) return 'S';
        if (c2 > c1) return 'L';
        if (c2 < c1) return 'O';
        return ' ';
    }

    public void atuar() {
        String posAtual = getPosicaoString(ambiente.agenteR, ambiente.agenteC);
        visitados.add(posAtual);
        boolean todasComidasColetadas = ambiente.getComidasColetadas() >= ambiente.getTotalComidasIniciais();

        int[][] visaoLongoAlcance = ambiente.getSensorDeVarredura();
        int maxComidas = 0;
        char melhorDirecaoComida = ' ';
        
        if (visaoLongoAlcance[0][1] > maxComidas) { maxComidas = visaoLongoAlcance[0][1]; melhorDirecaoComida = 'N'; } // Norte
        if (visaoLongoAlcance[2][1] > maxComidas) { maxComidas = visaoLongoAlcance[2][1]; melhorDirecaoComida = 'S'; } // Sul
        if (visaoLongoAlcance[1][2] > maxComidas) { maxComidas = visaoLongoAlcance[1][2]; melhorDirecaoComida = 'L'; } // Leste
        if (visaoLongoAlcance[1][0] > maxComidas) { maxComidas = visaoLongoAlcance[1][0]; melhorDirecaoComida = 'O'; } // Oeste

        char[][] sensorImediato = ambiente.getSensor();
        char[] direcoes = {'N', 'S', 'L', 'O'};
        List<Character> movimentosNaoVisitados = new ArrayList<>();
        char direcaoSaida = ' ';

        for (char dir : direcoes) {
            int r = -1, c = -1;
            if (dir == 'N') { r = 0; c = 1; } else if (dir == 'S') { r = 2; c = 1; }
            else if (dir == 'L') { r = 1; c = 2; } else if (dir == 'O') { r = 1; c = 0; }

            char conteudo = sensorImediato[r][c];

            if (conteudo == 'S' && todasComidasColetadas) {
                direcaoSaida = dir;
            }
            
            if (conteudo != 'X') {
                int[] coordsVizinho = {ambiente.agenteR, ambiente.agenteC};
                if (dir == 'N') coordsVizinho[0]--; else if (dir == 'S') coordsVizinho[0]++;
                else if (dir == 'L') coordsVizinho[1]++; else if (dir == 'O') coordsVizinho[1]--;
                
                String posVizinha = getPosicaoString(coordsVizinho[0], coordsVizinho[1]);
                if (!visitados.contains(posVizinha)) {
                    movimentosNaoVisitados.add(dir);
                }
            }
        }

        char proximaDirecao = ' ';
        boolean isBacktracking = false;

        if (todasComidasColetadas && direcaoSaida != ' ') {
            proximaDirecao = direcaoSaida;
        
        } else if (maxComidas > 0) {
            proximaDirecao = melhorDirecaoComida;
            int r = -1, c = -1;
            if (proximaDirecao == 'N') { r = 0; c = 1; } else if (proximaDirecao == 'S') { r = 2; c = 1; }
            else if (proximaDirecao == 'L') { r = 1; c = 2; } else if (proximaDirecao == 'O') { r = 1; c = 0; }
            if (r != -1 && sensorImediato[r][c] == 'X') {
                proximaDirecao = ' '; 
            }
        }
   
        if (proximaDirecao == ' ' && !movimentosNaoVisitados.isEmpty()) {
            proximaDirecao = movimentosNaoVisitados.get(0);
        
        } else if (proximaDirecao == ' ' && !pilhaCaminho.isEmpty()) {
            String[] posDestinoCoords = pilhaCaminho.pop().split(",");
            int rDestino = Integer.parseInt(posDestinoCoords[0]);
            int cDestino = Integer.parseInt(posDestinoCoords[1]);
            proximaDirecao = getDirecaoPara(ambiente.agenteR, ambiente.agenteC, rDestino, cDestino);
            isBacktracking = true;
        }

        if (proximaDirecao != ' ') {
            if (!isBacktracking) {
                pilhaCaminho.push(posAtual);
            }
            ambiente.setDirection(proximaDirecao);
            ambiente.move();
        } else {
            System.out.println("Agente Paralisado.");
        }
    }
}