package robo;
import comum.*;
import java.util.*;
public final class Corpo {
    public double energia=100,integridade=100,tedio=0,recompensa=0;
    public Emocao emocao=Emocao.CALMA;
    public double impulso() { return (100-energia)/100+1.5*(100-integridade)/100+tedio/100; }
    private double limitar(double valor) { return Math.max(0,Math.min(100,valor)); }
    public void sentir(ResultadoAcao resultado,boolean nova) {
        double antes=impulso();
        energia=limitar(energia-1-(resultado.colidiu?1:0)+(resultado.pedraRecolhida?15:0));
        integridade=limitar(integridade+(resultado.colidiu?-25:2));
        if(resultado.moveu) tedio=limitar(tedio+(nova?-30:10));
        recompensa=antes-impulso();
        emocao=resultado.colidiu||integridade<60?Emocao.MEDO:energia<25?Emocao.ANGUSTIA:
            recompensa>0.15?Emocao.ALEGRIA:tedio>=50?Emocao.TEDIO:Emocao.CALMA;
    }
    public EstadoInterno estado(String decisao) {
        Map<String,Double> variaveis=new LinkedHashMap<String,Double>();
        variaveis.put("energia",energia); variaveis.put("integridade",integridade); variaveis.put("tedio",tedio);
        variaveis.put("temperatura",emocao.temperatura); variaveis.put("deliberacao",decisao.equals("MEMORIA")?1.0:0.0);
        return new EstadoInterno(variaveis,emocao.name(),-impulso(),recompensa);
    }
}
