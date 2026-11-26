package robo;
public enum Emocao {
    MEDO(0.05), ANGUSTIA(0.05), ALEGRIA(0.10), TEDIO(0.30), CALMA(0.10);
    public final double temperatura;
    Emocao(double temperatura) { this.temperatura=temperatura; }
}
