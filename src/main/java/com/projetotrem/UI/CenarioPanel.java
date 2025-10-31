package com.projetotrem.UI;

import com.projetotrem.sync.GerenteDaEstacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;

public class CenarioPanel extends JPanel implements ActionListener {

    private GerenteDaEstacao gerente;

    private Image imgBackground;
    private Image imgTrain;
    private Image imgTrain_Flipped;
    private Image imgPackerIdle;
    private Image imgPackerWorking;

    private Image imgHudPackerIniciado;
    private Image imgHudPackerEmpacotando;
    private Image imgHudPackerInterrompido;

    private int trainX;
    private int trainY;
    private int trainTargetX;
    private int TRAIN_SPEED; // Pixels por "tick"
    private boolean isTrainFacingLeft = false;

    private final int Y_POS_BASE = 650;//900;           // Posição para "Dormindo" ou outro estado
    private final int Y_POS_ESPERANDO_VAGA = 660;//850; // Posição para "Esperando vaga"
    private final int Y_POS_EMPACOTANDO = 610;//792;    // Posição para "Empacotando" (mais acima)

    private final int ESTEIRA_START_X = 10;
    private final int ESTEIRA_END_X = 480;

    private final int LARGURA_BASE = 90;
    private final int ALTURA_BASE = 90;

    private final int LARGURA_ESPERANDO = 150;
    private final int ALTURA_ESPERANDO = 150;

    private final int LARGURA_EMPACOTANDO = 100;
    private final int ALTURA_EMPACOTANDO = 100;

    private final int TREM_LARGURA_DESEJADA = 350;
    private final int TREM_ALTURA_DESEJADA = 300;

    private int capacidadeDoTrem;
    private int capacidadeDoDeposito;

    private final long tempoViagem;
    private int posA;
    private int posB = -1;

    private long animationStartTime; // Relógio: quando a animação (viagem) começou
    private String lastKnownStatus = "";

    private Map<Integer, String> packerStatusMap;

    // Timer para a animação
    private Timer timer;

    public CenarioPanel(GerenteDaEstacao gerente, long tempoViagem) {
        this.gerente = gerente;
        this.packerStatusMap = new HashMap<>();
        this.tempoViagem = tempoViagem;
        this.capacidadeDoTrem = gerente.getCapacidadeDoTrem();
        this.capacidadeDoDeposito = gerente.getCapacidadeDoDeposito();

        loadImages();

        setPreferredSize(new Dimension(1200, 1000));

        this.posA = -TREM_LARGURA_DESEJADA;
        this.trainX = posA;

        int painelAltura = 600;
        int tremAltura = TREM_ALTURA_DESEJADA;
        this.trainY = (painelAltura / 2) - (tremAltura / 2);
        int distanciaTotal = 1200 + TREM_LARGURA_DESEJADA;

        // Ticks de 16ms (aprox. 60fps)
        int tickMs = 16;

        // Quantos "ticks" a animação tem para completar a viagem
        // (Ex: 5000ms / 16ms = 312.5 ticks)
        int totalTicks = (int) (tempoViagem / tickMs);

        // Velocidade = Distância / Tempo
        // (Ex: 1550 pixels / 312.5 ticks = 4.96 pixels/tick)
        this.TRAIN_SPEED = Math.max(1, (int) Math.ceil((double) distanciaTotal / totalTicks));

        System.out.println("Tempo de Viagem: " + tempoViagem + "ms");
        System.out.println("Distância Total: " + distanciaTotal + "px");
        System.out.println("Ticks Totais: " + totalTicks);
        System.out.println("Velocidade Calculada: " + this.TRAIN_SPEED + " pixels/tick");

        timer = new Timer(tickMs, this);
        timer.start();
    }

    private void loadImages() {
        imgBackground = loadImage("/images/paisagem.jpg");
        imgPackerIdle = loadImage("/images/Psleep.png");
        Image imgTrainOriginal = loadImage("/images/trem.png");
        imgTrain = imgTrainOriginal.getScaledInstance(TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, Image.SCALE_SMOOTH);
        imgPackerWorking = loadImage("/images/Ppacking.png");
        Image imgTrainOriginal_Right = loadImage("/images/trem.png");
        imgTrain = imgTrainOriginal_Right.getScaledInstance(TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, Image.SCALE_SMOOTH);

        Image imgTrainOriginal_Left = loadImage("/images/tremInvertido.png");
        imgTrain_Flipped = imgTrainOriginal_Left.getScaledInstance(TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, Image.SCALE_SMOOTH);

        imgHudPackerIniciado = loadImage("/images/dormindo.png").getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        imgHudPackerEmpacotando = loadImage("/images/empacotando.png").getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        imgHudPackerInterrompido = loadImage("/images/iniciado.png").getScaledInstance(60, 60, Image.SCALE_SMOOTH);
    }

    // Método helper para carregar imagens (trata erros)
    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Erro: Não foi possível carregar a imagem: " + path);

            return new ImageIcon().getImage();
        }
        return new ImageIcon(url).getImage();
    }

    /**
     * Este é o coração do Timer.
     * Chamado a cada 16ms.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        updateVisualState();

        repaint();
    }

    private void updateVisualState() {
        if (posB == -1) {
            if (getWidth() == 0) return;

            posB = getWidth();
            System.out.println("Posição B (tela) inicializada para: " + posB);
        }

        String statusTrem = gerente.getStatusDoTrem();

        if (!statusTrem.equals(lastKnownStatus)) {
            System.out.println("STATUS MUDOU: de '" + lastKnownStatus + "' para '" + statusTrem + "'");

            // REINICIA O RELÓGIO DA ANIMAÇÃO!
            animationStartTime = System.currentTimeMillis();
            lastKnownStatus = statusTrem;
        }

        long timeElapsed = System.currentTimeMillis() - animationStartTime;

        double percentComplete = Math.min(1.0, (double) timeElapsed / this.tempoViagem);

        if (statusTrem.equals("Viajando (A -> B)")) {
            isTrainFacingLeft = false;

            trainX = (int) (posA + ( (posB - posA) * percentComplete) );

        } else if (statusTrem.equals("Retornando (B -> A)")) {
            isTrainFacingLeft = true;

            trainX = (int) (posB - ( (posB - posA) * percentComplete) );

        } else {
            isTrainFacingLeft = false;
            trainX = posA;
        }

        trainX = Math.max(posA, Math.min(posB, trainX));

        this.packerStatusMap = gerente.getStatusDosEmpacotadores();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Importante!

        Graphics2D g2d = (Graphics2D) g;

        // 1. Desenhar o fundo
        g2d.drawImage(imgBackground, 0, 0, getWidth(), getHeight(), this);


        // 2. Desenhar o Trem
        Image imagemDoTremAtual;

        if (isTrainFacingLeft) {
            imagemDoTremAtual = imgTrain_Flipped;
        } else {
            imagemDoTremAtual = imgTrain;
        }
        g2d.drawImage(imagemDoTremAtual, trainX, trainY, TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, this);

        // 3. Desenhar os Empacotadores
        if (packerStatusMap == null || packerStatusMap.isEmpty()) {
            drawStatusHUD(g2d);
            return;
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        Map<Integer, String> sortedPackerMap = new TreeMap<>(packerStatusMap);

        int numPackers = sortedPackerMap.size();
        int esteiraLarguraUtil = ESTEIRA_END_X - ESTEIRA_START_X;

        int espacamento;
        if (numPackers > 1) {
            espacamento = esteiraLarguraUtil / (numPackers - 1);
        } else {
            espacamento = 0;
        }

        int packerIndex = 0;

        for (Map.Entry<Integer, String> entry : sortedPackerMap.entrySet()) {
            int id = entry.getKey();
            String status = entry.getValue();

            int drawX = ESTEIRA_START_X + (packerIndex * espacamento);

            Image packerImage;
            int drawY;
            int drawWidth;
            int drawHeight;

            if (status.equals("Empacotando")) {
                packerImage = imgPackerWorking;
                drawY = Y_POS_EMPACOTANDO;
                drawWidth = LARGURA_EMPACOTANDO;
                drawHeight = ALTURA_EMPACOTANDO;

            } else if (status.equals("Esperando vaga")) {
                packerImage = imgPackerIdle;
                drawY = Y_POS_ESPERANDO_VAGA;
                drawWidth = LARGURA_ESPERANDO;
                drawHeight = ALTURA_ESPERANDO;

            } else {
                packerImage = imgPackerIdle;
                drawY = Y_POS_BASE;
                drawWidth = LARGURA_BASE;
                drawHeight = ALTURA_BASE;
            }

            g2d.drawImage(packerImage, drawX, drawY, drawWidth, drawHeight, this);

            g2d.drawString(String.format("[%d] %s", id, status), drawX, drawY + drawHeight + 15);

            packerIndex++;
        }

        drawStatusHUD(g2d);
    }

    private Image getPackerHudImage(String status) {
        if (status == null) {
            return imgHudPackerInterrompido;
        }

        switch (status) {
            case "Empacotando":
                return imgHudPackerEmpacotando;
            case "Interrompido":
                return imgHudPackerInterrompido;

            case "Iniciando":
            case "Esperando vaga":
            case "Armazenando no Deposito":
            default:
                return imgHudPackerIniciado;
        }
    }


    private void drawStatusHUD(Graphics2D g2d) {

        int caixasArmazem = gerente.getQuantidadeDeCaixasDepositadas();
        String statusTrem = gerente.getStatusDoTrem();

        int caixasTrem = 0;
        if (statusTrem.contains("Viajando") || statusTrem.contains("Retornando")) {
            caixasTrem = this.capacidadeDoTrem;
        }

        g2d.setColor(new Color(0, 0, 0, 150));

        int hudHeight = 120;
        g2d.fillRect(0, 0, getWidth(), hudHeight);

        g2d.setColor(Color.WHITE);
        int xPadding = 5;
        int yPos = 25;

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(String.format("Armazém: %d / %d", caixasArmazem, this.capacidadeDoDeposito), xPadding, yPos);
        yPos += 25;
        g2d.drawString(String.format("Carga no Trem: %d / %d", caixasTrem, this.capacidadeDoTrem), xPadding, yPos);
        yPos += 25;
        g2d.drawString(String.format("Status Trem: %s", statusTrem), xPadding, yPos);

        int xPosPackers = 350;
        yPos = 25;

        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        int yStart = 25;

        g2d.drawString(String.format("Armazém: %d / %d", caixasArmazem, this.capacidadeDoDeposito), xPadding, yStart);
        g2d.drawString(String.format("Carga no Trem: %d / %d", caixasTrem, this.capacidadeDoTrem), xPadding, yStart + 25);
        g2d.drawString(String.format("Status Trem: %s", statusTrem), xPadding, yStart + 50);


        int startXPackers = 350;
        int currentX = startXPackers;
        int currentY = 15;


        int iconWidth = 60;
        int iconHeight = 60;

        int horizontalSpacing = 200;

        int verticalTextOffset = iconHeight + 5;

        Font fontNome = new Font("Arial", Font.BOLD, 12);
        Font fontStatus = new Font("Monospaced", Font.PLAIN, 10);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

        if (packerStatusMap != null) {
            List<Map.Entry<Integer, String>> sortedPackers =
                    new ArrayList<>(packerStatusMap.entrySet());
            sortedPackers.sort(Map.Entry.comparingByKey());
            Map<Integer, String> sortedMapForHUD = new TreeMap<>(packerStatusMap);

            int count = 0;
            for (Map.Entry<Integer, String> entry : sortedPackers) {
                if (count >= 4) break;

                int id = entry.getKey();
                String status = entry.getValue();
                Image img = getPackerHudImage(status);

                g2d.drawImage(img, currentX, currentY, iconWidth, iconHeight, this);

                g2d.setFont(fontNome);
                g2d.setColor(Color.WHITE);
                String nome = String.format("Empacotador %d", id);
                int nomeWidth = g2d.getFontMetrics(fontNome).stringWidth(nome);
                int nomeX = currentX + (iconWidth - nomeWidth) / 2;
                g2d.drawString(nome, nomeX, currentY + verticalTextOffset);

                g2d.setFont(fontStatus);
                g2d.setColor(Color.LIGHT_GRAY);
                int statusWidth = g2d.getFontMetrics(fontStatus).stringWidth(status);
                int statusX = currentX + (iconWidth - statusWidth) / 2;
                g2d.drawString(status, statusX, currentY + verticalTextOffset + 15);

                currentX += horizontalSpacing;
                count++;
            }
        }
    }
}
