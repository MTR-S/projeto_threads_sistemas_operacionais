package com.projetotrem.UI;

import com.projetotrem.sync.GerenteDaEstacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator; // Importar Comparator

public class CenarioPanel extends JPanel implements ActionListener {

    // --- Backend ---
    private GerenteDaEstacao gerente;

    // --- Imagens (PLACEHOLDERS) ---
    private Image imgBackground;
    private Image imgTrain;

    // --- (NOVO) Imagens do HUD ---
    private Image imgHudPackerIniciado;
    private Image imgHudPackerEmpacotando;
    private Image imgHudPackerInterrompido;

    // --- Estado da Animação ---
    private int trainX;           // Posição X atual do trem
    private int trainY;           // Posição Y atual do trem
    private int trainTargetX;     // Posição X para onde o trem quer ir
    private final int TRAIN_SPEED = 5; // Pixels por "tick"

    // [REMOVIDO] Posições dos empacotadores antigos
    
    private final int TREM_LARGURA_DESEJADA = 350;
    private final int TREM_ALTURA_DESEJADA = 300;

    private int capacidadeDoTrem;
    private int capacidadeDoDeposito;

    // Mapa para guardar o status de cada empacotador
    private Map<Integer, String> packerStatusMap;

    // Timer para a animação
    private Timer timer;

    public CenarioPanel(GerenteDaEstacao gerente) {
        this.gerente = gerente;
        this.packerStatusMap = new HashMap<>();

        this.capacidadeDoTrem = gerente.getCapacidadeDoTrem();
        this.capacidadeDoDeposito = gerente.getCapacidadeDoDeposito();

        // 1. Carregar as imagens
        loadImages();

        // 2. Definir o tamanho preferido do painel
        // Use o tamanho da sua imagem de fundo!
        // EXEMPLO: se seu fundo for 800x600
        setPreferredSize(new Dimension(1200, 1000));

        // 3. Definir posições iniciais
        this.trainX = 0; // Posição A (Esquerda)
        this.trainTargetX = 0;

        // --- CÁLCULO PARA CENTRALIZAR O TREM ---

        // 1. Pega a altura do painel (que definimos no setPreferredSize)
        int painelAltura = 600;

        // 2. Pega a altura da imagem do trem (que já foi carregada)
        // (Usamos 'null' como ImageObserver, o que é ok aqui)
        int tremAltura = imgTrain.getHeight(null);

        // 3. Calcula a posição Y para o centro:
        // (Metade da Tela) - (Metade do Trem)
        this.trainY = (painelAltura / 2) - (tremAltura / 2) - 140;

        // 4. Iniciar o Timer de animação
        // "Dispara" a cada 16ms (aprox. 60 FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    private void loadImages() {
        imgBackground = loadImage("/images/paisagem.jpg");
        Image imgTrainOriginal = loadImage("/images/trem.png");
        imgTrain = imgTrainOriginal.getScaledInstance(TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, Image.SCALE_SMOOTH);

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
        // --- Atualizar Trem ---
        String statusTrem = gerente.getStatusDoTrem();

        // Define o "alvo" do trem baseado no status
        if (statusTrem.equals("Viajando (A -> B)")) {
            // Alvo = Lado direito da tela
            trainTargetX = getWidth() - imgTrain.getWidth(null);
        } else if (statusTrem.equals("Retornando (B -> A)")) {
            // Alvo = Lado esquerdo da tela
            trainTargetX = 0;
        } else if (statusTrem.equals("Esperando carregamento!") || statusTrem.equals("Carregando...")) {
            // Alvo = Lado esquerdo (Estação A)
            trainTargetX = 0;
        }

        // Move o trem suavemente em direção ao alvo
        if (trainX < trainTargetX) {
            trainX = Math.min(trainX + TRAIN_SPEED, trainTargetX);
        } else if (trainX > trainTargetX) {
            trainX = Math.max(trainX - TRAIN_SPEED, trainTargetX);
        }

        // --- Atualizar Empacotadores ---
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
        g2d.drawImage(imgTrain, trainX, trainY, TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, this);

        // 3. [REMOVIDO] Bloco de desenho dos empacotadores antigos
        // ...

        // 4. Desenhar o HUD no topo
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
        g2d.setColor(new Color(0, 0, 0, 150)); // Cor preta, 60% transparente
        int hudHeight = 120;
        g2d.fillRect(0, 0, getWidth(), hudHeight); // Desenha a barra no topo

        // --- 3. Desenhar Texto da Coluna 1 ---
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        int xPadding = 10; // Espaçamento da borda esquerda
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

        if (packerStatusMap != null) {
            // 1. Coletar e ordenar as entradas por ID (chave)
            List<Map.Entry<Integer, String>> sortedPackers =
                    new ArrayList<>(packerStatusMap.entrySet());
            sortedPackers.sort(Map.Entry.comparingByKey());

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