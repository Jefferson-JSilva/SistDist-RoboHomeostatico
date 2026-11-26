package ambiente;
import comum.*;
import infra.Movimento;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
public final class Mapa {
    final char[][] celulas=new char[6][6];
    final Pose inicio;
    final int livres, pedras;
    public Mapa(Path arquivo) throws IOException {
        List<String[]> linhas=new ArrayList<String[]>();
        for(String texto:Files.readAllLines(arquivo,StandardCharsets.UTF_8)) {
            texto=texto.replaceFirst("//.*$", "").trim();
            if(!texto.isEmpty()) linhas.add(texto.split("\\s+"));
        }
        if(linhas.size()!=6) throw new IOException("Mapa deve ter seis linhas");
        Pose posicao=null; int totalLivres=0,totalPedras=0;
        for(int l=0;l<6;l++) {
            if(linhas.get(l).length!=6) throw new IOException("Mapa deve ter seis colunas");
            for(int c=0;c<6;c++) {
                String valor=linhas.get(l)[c];
                if(valor.length()!=1||"R#.P".indexOf(valor.charAt(0))<0) throw new IOException("Celula invalida");
                char tipo=valor.charAt(0); celulas[l][c]=tipo;
                if(tipo!='#') totalLivres++;
                if(tipo=='P') totalPedras++;
                if(tipo=='R') { if(posicao!=null) throw new IOException("Mais de uma origem"); posicao=new Pose(l,c,Direcao.NORTE); }
            }
        }
        if(posicao==null) throw new IOException("Falta origem R");
        inicio=posicao; livres=totalLivres; pedras=totalPedras;
        // Evita um denominador impossivel: todos os locais devem ser alcancaveis.
        Set<Integer> vistos=new HashSet<Integer>(); Queue<Integer> fila=new ArrayDeque<Integer>();
        fila.add(inicio.linha*6+inicio.coluna); vistos.add(fila.peek());
        while(!fila.isEmpty()) { int p=fila.remove(); for(int d=0;d<4;d++) {
            int l=p/6+Movimento.LINHAS[d],c=p%6+Movimento.COLUNAS[d];
            if(!bloqueada(l,c)&&vistos.add(l*6+c)) fila.add(l*6+c);
        } }
        if(vistos.size()!=livres) throw new IOException("Mapa contem celulas inacessiveis");
    }
    boolean bloqueada(int linha,int coluna) { return !Movimento.dentro(linha,coluna)||celulas[linha][coluna]=='#'; }
}
