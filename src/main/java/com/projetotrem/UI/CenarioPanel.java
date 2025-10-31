package com.projetotrem.UI;

import com.projetotrem.sync.GerenteDaEstacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap; // IMPORTANTE: Adicionado para ordenar os empacotadores
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator; // Importar Comparator

public class CenarioPanel extends JPanel implements ActionListener {

    // --- Backend ---
    private GerenteDaEstacao gerente;

    // --- Imagens (PLACEHOLDERS) ---
    private Image imgBackground;
    private Image imgTrain;
    private Image imgTrain_Flipped;
    private Image imgPackerIdle; // Imagem do empacotador parado
    private Image imgPackerWorking; // Imagem dele trabalhando

    // --- (NOVO) Imagens do HUD ---
    private Image imgHudPackerIniciado;
    private Image imgHudPackerEmpacotando;
    private Image imgHudPackerInterrompido;

    // --- Estado da Animação ---
    private int trainX;             // Posição X atual do trem
    private int trainY;             // Posição Y atual do trem
    private int trainTargetX;       // Posição X para onde o trem quer ir
    private int TRAIN_SPEED; // Pixels por "tick"
    private boolean isTrainFacingLeft = false;

    // --- [NOVA LÓGICA DE POSICIONAMENTO E TAMANHO] ---

    // 1. Posições Y (verticais) baseadas no status
    // (Valores do seu código mais recente)
    private final int Y_POS_BASE = 900;           // Posição para "Dormindo" ou outro estado
    private final int Y_POS_ESPERANDO_VAGA = 850; // Posição para "Esperando vaga"
    private final int Y_POS_EMPACOTANDO = 792;    // Posição para "Empacotando" (mais acima)

    // 2. Área da "Esteira" para Posições X (horizontais)
    private final int ESTEIRA_START_X = 10; // Onde o primeiro empacotador começa
    private final int ESTEIRA_END_X = 480;   // Onde o último empacotador termina

    // 3. (MERGE) TAMANHOS baseados no status
    // (Valores da nossa discussão anterior, você pode ajustar)
    private final int LARGURA_BASE = 90;
    private final int ALTURA_BASE = 90;

    // Como o Y é o mesmo, vou manter o tamanho igual
    private final int LARGURA_ESPERANDO = 150;
    private final int ALTURA_ESPERANDO = 150;

    // Tamanho que você usava antes (100x100)
    private final int LARGURA_EMPACOTANDO = 100;
    private final int ALTURA_EMPACOTANDO = 100;

    // --- [FIM DA NOVA LÓGICA] ---

    // [REMOVIDO] Posições dos empacotadores antigos

    private final int TREM_LARGURA_DESEJADA = 350;
    private final int TREM_ALTURA_DESEJADA = 300;

    private int capacidadeDoTrem;
    private int capacidadeDoDeposito;

    private final long tempoViagem; // Tempo total da viagem (vem do construtor)
    private int posA;           // Posição X de início (fora da tela)
    private int posB = -1;      // Posição X final (fora da tela) - Inicia em -1

    private long animationStartTime; // Relógio: quando a animação (viagem) começou
    private String lastKnownStatus = "";

    // Mapa para guardar o status de cada empacotador
    private Map<Integer, String> packerStatusMap;

    // Timer para a animação
    private Timer timer;

    public CenarioPanel(GerenteDaEstacao gerente, long tempoViagem) {
        this.gerente = gerente;
        this.packerStatusMap = new HashMap<>();
        this.tempoViagem = tempoViagem;
        this.capacidadeDoTrem = gerente.getCapacidadeDoTrem();
        this.capacidadeDoDeposito = gerente.getCapacidadeDoDeposito();

        // 1. Carregar as imagens
        loadImages();

        // 2. Definir o tamanho preferido do painel
        // Use o tamanho da sua imagem de fundo!
        // EXEMPLO: se seu fundo for 800x600
        setPreferredSize(new Dimension(1200, 1000));

        // 3. Definir posições iniciais
        this.posA = -TREM_LARGURA_DESEJADA;
        this.trainX = posA;

        // --- CÁLCULO PARA CENTRALIZAR O TREM ---

        // 1. Pega a altura do painel (que definimos no setPreferredSize)
                int painelAltura = 600;

        // 2. Pega a altura da imagem do trem (que já foi carregada)
        // (Usamos 'null' como ImageObserver, o que é ok aqui)
        int tremAltura = TREM_ALTURA_DESEJADA;

        // 3. Calcula a posição Y para o centro:
        // (Metade da Tela) - (Metade do Trem)
                this.trainY = (painelAltura / 2) - (tremAltura / 2) + 20;

        // 4. Iniciar o Timer de animação
        int distanciaTotal = 1200 + TREM_LARGURA_DESEJADA;

        // Ticks de 16ms (aprox. 60fps)
        int tickMs = 16;

        // Quantos "ticks" a animação tem para completar a viagem
        // (Ex: 5000ms / 16ms = 312.5 ticks)
        int totalTicks = (int) (tempoViagem / tickMs);

        // Velocidade = Distância / Tempo
        // (Ex: 1550 pixels / 312.5 ticks = 4.96 pixels/tick)
        // Usamos Math.ceil e Math.max(1, ...) para garantir que a velocidade
        // seja sempre pelo menos 1 e um número inteiro.
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
        Image imgTrainOriginal_Right = loadImage("/images/trem.png"); // Supondo que 'trem.png' seja para a direita
        imgTrain = imgTrainOriginal_Right.getScaledInstance(TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, Image.SCALE_SMOOTH);

        // 2. Carrega a imagem do trem virada para a esquerda (a que você já inverteu e salvou)
        Image imgTrainOriginal_Left = loadImage("/images/tremInvertido.png"); // Supondo que 'trem_flipped.png' seja a invertida
        imgTrain_Flipped = imgTrainOriginal_Left.getScaledInstance(TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, Image.SCALE_SMOOTH);

        // [REMOVIDO] Carregamento das imagens antigas (e_dormindo, e_trabalhando)

        // --- Carregar imagens do HUD ---
        // (Usando os caminhos que você forneceu e um tamanho maior: 60x60)
        imgHudPackerIniciado = loadImage("/images/dormindo.png").getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        imgHudPackerEmpacotando = loadImage("/images/empacotando.png").getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        imgHudPackerInterrompido = loadImage("/images/iniciado.png").getScaledInstance(60, 60, Image.SCALE_SMOOTH);
    }

    // Método helper para carregar imagens (trata erros)
    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Erro: Não foi possível carregar a imagem: " + path);
            // Retorna uma imagem "placeholder" de erro
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
        // 1. Atualiza o estado da lógica (o que queremos desenhar)
        updateVisualState();

        // 2. Manda o Swing redesenhar o painel
        repaint();
    }

    /**
     * Pega os dados do Gerente (backend) e atualiza
     * as variáveis de animação (frontend).
     */
    private void updateVisualState() {
        // --- 0. Inicializar Posição B (se ainda não foi feito) ---
        // Não podemos usar getWidth() no construtor (dá 0).
        // Fazemos isso aqui, no primeiro tick.
        if (posB == -1) {
            if (getWidth() == 0) return; // Espera o painel estar pronto

            // getWidth() é a largura da tela (1200)
            // posB é o trem "saindo" da tela
            posB = getWidth();
            System.out.println("Posição B (tela) inicializada para: " + posB);
        }

        // --- 1. Ler o status do Backend ---
        String statusTrem = gerente.getStatusDoTrem();

        // --- 2. Detectar MUDANÇA de Status ---
        // O status mudou (ex: de "Carregando" para "Viajando")?
        if (!statusTrem.equals(lastKnownStatus)) {
            System.out.println("STATUS MUDOU: de '" + lastKnownStatus + "' para '" + statusTrem + "'");

            // REINICIA O RELÓGIO DA ANIMAÇÃO!
            animationStartTime = System.currentTimeMillis();
            lastKnownStatus = statusTrem;
        }

        // --- 3. Calcular Posição Baseada no TEMPO ---

        // Quanto tempo (ms) passou desde que a animação (viagem) começou?
        long timeElapsed = System.currentTimeMillis() - animationStartTime;

        // Qual o percentual completo da viagem? (de 0.0 a 1.0)
        // Usamos Math.min(1.0, ...) para travar em 100% (1.0)
        double percentComplete = Math.min(1.0, (double) timeElapsed / this.tempoViagem);

        // --- 4. Definir a Posição X baseado no Percentual ---

        if (statusTrem.equals("Viajando (A -> B)")) {
            isTrainFacingLeft = false;

            // Posição = (Início) + (Distância Total * Percentual)
            trainX = (int) (posA + ( (posB - posA) * percentComplete) );

        } else if (statusTrem.equals("Retornando (B -> A)")) {
            isTrainFacingLeft = true;

            // Posição = (Fim) - (Distância Total * Percentual)
            trainX = (int) (posB - ( (posB - posA) * percentComplete) );

        } else { // "Esperando", "Carregando", etc.
            isTrainFacingLeft = false;
            trainX = posA; // Trava o trem na Posição A (fora da tela)
        }

        // Garante que o trem não "passe" do limite
        trainX = Math.max(posA, Math.min(posB, trainX));

        // --- 5. Atualizar Empacotadores (como antes) ---
        this.packerStatusMap = gerente.getStatusDosEmpacotadores();
    }

    /**
     * Este é o coração do Desenho!
     * O Swing chama este método sempre que 'repaint()' é invocado.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Importante!

        Graphics2D g2d = (Graphics2D) g; // Melhor para desenho 2D

        // 1. Desenhar o Fundo
        // Desenha o fundo para preencher o painel
        g2d.drawImage(imgBackground, 0, 0, getWidth(), getHeight(), this);

        // 2. Desenhar o Trem
        // Desenha o trem na sua posição X/Y atual
        Image imagemDoTremAtual;
        if (isTrainFacingLeft) {
            imagemDoTremAtual = imgTrain_Flipped;
        } else {
            imagemDoTremAtual = imgTrain; // O normal, virado para a direita
        }
        g2d.drawImage(imagemDoTremAtual, trainX, trainY, TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, this);

        // --- [LÓGICA DE DESENHO DINÂMICO DOS EMPACOTADORES] ---

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
            espacamento = 0; // Só há um empacotador
        }

        int packerIndex = 0; // Índice de 0 a N-1

        for (Map.Entry<Integer, String> entry : sortedPackerMap.entrySet()) {
            int id = entry.getKey();
            String status = entry.getValue();

            // --- Lógica de Posição X (Dinâmica) ---
            int drawX = ESTEIRA_START_X + (packerIndex * espacamento);

            // --- (MERGE) Lógica de Posição Y, Imagem e TAMANHO (Dinâmica) ---
            Image packerImage;
            int drawY;
            int drawWidth;  // <- NOVA VARIÁVEL
            int drawHeight; // <- NOVA VARIÁVEL

            if (status.equals("Empacotando")) {
                packerImage = imgPackerWorking;
                drawY = Y_POS_EMPACOTANDO;       // (Seu valor: 792)
                drawWidth = LARGURA_EMPACOTANDO; // (Valor do merge: 100)
                drawHeight = ALTURA_EMPACOTANDO;// (Valor do merge: 100)

            } else if (status.equals("Esperando vaga")) {
                packerImage = imgPackerIdle;
                drawY = Y_POS_ESPERANDO_VAGA;    // (Seu valor: 900)
                drawWidth = LARGURA_ESPERANDO;   // (Valor do merge: 90)
                drawHeight = ALTURA_ESPERANDO;  // (Valor do merge: 90)

            } else {
                // Default: "Dormindo", "Idle", etc.
                packerImage = imgPackerIdle;
                drawY = Y_POS_BASE;              // (Seu valor: 900)
                drawWidth = LARGURA_BASE;        // (Valor do merge: 90)
                drawHeight = ALTURA_BASE;       // (Valor do merge: 90)
            }
            // --- Fim da Lógica ---


            // (MERGE) Desenha a imagem com tamanho dinâmico
            g2d.drawImage(packerImage, drawX, drawY, drawWidth, drawHeight, this);

            // (MERGE) Desenha o status relativo ao tamanho dinâmico
            g2d.drawString(String.format("[%d] %s", id, status), drawX, drawY + drawHeight + 15);

            packerIndex++; // Incrementa o índice para o próximo empacotador
        }

        // --- [FIM DA LÓGICA DE DESENHO] ---

        drawStatusHUD(g2d);
    }

    // --- (NOVO) Método Helper ---
    /**
     * Seleciona a imagem correta do empacotador para o HUD com base no status.
     * Mapeia os status do backend para as imagens carregadas.
     */
    private Image getPackerHudImage(String status) {
        if (status == null) {
            return imgHudPackerInterrompido; // Imagem de erro/nulo
        }

        switch (status) {
            case "Empacotando":
                return imgHudPackerEmpacotando;
            case "Interrompido":
                return imgHudPackerInterrompido;

            // Mapeia outros status "ativos" para a imagem "iniciado"
            case "Iniciando":
            case "Esperando vaga":
            case "Armazenando no Deposito":
            default:
                return imgHudPackerIniciado;
        }
    }


    private void drawStatusHUD(Graphics2D g2d) {

        // --- 1. Coletar Dados ---
        int caixasArmazem = gerente.getQuantidadeDeCaixasDepositadas();
        String statusTrem = gerente.getStatusDoTrem();

        int caixasTrem = 0;
        // Se o trem está viajando, ele está com a carga (N)
        if (statusTrem.contains("Viajando") || statusTrem.contains("Retornando")) {
            caixasTrem = this.capacidadeDoTrem;
        }

        // --- 2. Desenhar Fundo do HUD ---
        // Cor preta, 60% transparente (alpha 150 de 255)
        g2d.setColor(new Color(0, 0, 0, 150));

        // Define a altura do HUD (ex: 120 pixels)
        int hudHeight = 120;
        g2d.fillRect(0, 0, getWidth(), hudHeight);

        // --- 3. Desenhar Texto ---
        g2d.setColor(Color.WHITE);
        int xPadding = 5;
        int yPos = 25;

        // Coluna 1: Status Geral
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(String.format("Armazém: %d / %d", caixasArmazem, this.capacidadeDoDeposito), xPadding, yPos);
        yPos += 25;
        g2d.drawString(String.format("Carga no Trem: %d / %d", caixasTrem, this.capacidadeDoTrem), xPadding, yPos);
        yPos += 25;
        g2d.drawString(String.format("Status Trem: %s", statusTrem), xPadding, yPos);

        // Coluna 2: Status Empacotadores
        int xPosPackers = 350;
        yPos = 25;

        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        int yStart = 25;   // Posição Y inicial

        g2d.drawString(String.format("Armazém: %d / %d", caixasArmazem, this.capacidadeDoDeposito), xPadding, yStart);
        g2d.drawString(String.format("Carga no Trem: %d / %d", caixasTrem, this.capacidadeDoTrem), xPadding, yStart + 25);
        g2d.drawString(String.format("Status Trem: %s", statusTrem), xPadding, yStart + 50);

        // #############################################################
        // ### (INÍCIO DA REFATORAÇÃO) Coluna 2: Status Empacotadores ###
        // #############################################################

        // Posição inicial para o primeiro empacotador no HUD
        // (Ajustado para 350 para dar mais espaço ao texto do trem)
        int startXPackers = 350;
        int currentX = startXPackers;
        int currentY = 15; // Mais acima, para dar espaço para o texto abaixo da imagem

        // Dimensões do ícone e espaçamento
        // (Tamanho do ícone aumentado para 60x60 no loadImages)
        int iconWidth = 60;
        int iconHeight = 60;

        // (AJUSTADO) Espaçamento horizontal aumentado para 200px
        // O painel tem 1200px. A área de texto ~350px.
        // Sobram ~850px. 850 / 4 = ~212px por empacotador.
        // 200px de espaçamento total deve funcionar bem.
        int horizontalSpacing = 200;

        int verticalTextOffset = iconHeight + 5; // Espaço abaixo do ícone para o texto

        // Fontes
        Font fontNome = new Font("Arial", Font.BOLD, 12);
        Font fontStatus = new Font("Monospaced", Font.PLAIN, 10); // Fonte menor para status
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

        if (packerStatusMap != null) {
            // 1. Coletar e ordenar as entradas por ID (chave)
            List<Map.Entry<Integer, String>> sortedPackers =
                    new ArrayList<>(packerStatusMap.entrySet());
            sortedPackers.sort(Map.Entry.comparingByKey());
            Map<Integer, String> sortedMapForHUD = new TreeMap<>(packerStatusMap);

            // 2. Iterar e desenhar, limitado a 4
            int count = 0;
            for (Map.Entry<Integer, String> entry : sortedPackers) {
                if (count >= 4) break; // Limite de 4 empacotadores

                int id = entry.getKey();
                String status = entry.getValue();
                Image img = getPackerHudImage(status); // Pega a imagem correta

                // 3. Desenhar a Imagem do empacotador
                g2d.drawImage(img, currentX, currentY, iconWidth, iconHeight, this);

                // 4. Desenhar o Nome (Empacotador X) centralizado abaixo da imagem
                g2d.setFont(fontNome);
                g2d.setColor(Color.WHITE);
                String nome = String.format("Empacotador %d", id);
                int nomeWidth = g2d.getFontMetrics(fontNome).stringWidth(nome);
                int nomeX = currentX + (iconWidth - nomeWidth) / 2; // Centraliza o nome
                g2d.drawString(nome, nomeX, currentY + verticalTextOffset);

                // 5. Desenhar o Status (Empacotando/Iniciado/Interrompido) centralizado abaixo do nome
                g2d.setFont(fontStatus);
                g2d.setColor(Color.LIGHT_GRAY);
                int statusWidth = g2d.getFontMetrics(fontStatus).stringWidth(status);
                int statusX = currentX + (iconWidth - statusWidth) / 2; // Centraliza o status
                g2d.drawString(status, statusX, currentY + verticalTextOffset + 15); // +15 para ir para a próxima linha

                // Move para a próxima posição horizontal
                currentX += horizontalSpacing;
                count++;
            }
        }
        // ###########################################################
        // ### (FIM DA REFATORAÇÃO) Fim da Coluna 2                ###
        // ###########################################################
    }
}
