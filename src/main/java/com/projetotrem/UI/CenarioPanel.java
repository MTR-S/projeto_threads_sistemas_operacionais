package com.projetotrem.UI;

import com.projetotrem.sync.GerenteDaEstacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CenarioPanel extends JPanel implements ActionListener {

    // --- Backend ---
    private GerenteDaEstacao gerente;

    // --- Imagens (PLACEHOLDERS) ---
    private Image imgBackground;
    private Image imgTrain;
    private Image imgPackerIdle; // Imagem do empacotador parado
    private Image imgPackerWorking; // Imagem dele trabalhando

    // --- Estado da Animação ---
    private int trainX;             // Posição X atual do trem
    private int trainY;             // Posição Y atual do trem
    private int trainTargetX;       // Posição X para onde o trem quer ir
    private final int TRAIN_SPEED = 5; // Pixels por "tick"

    // Posições fixas dos empacotadores (EXEMPLO)
    // Você vai querer ajustar isso baseado no seu cenário
    private Point[] packerPositions = {
            new Point(100, 450),
            new Point(200, 450),
            new Point(300, 450)
            // Adicione mais pontos se precisar
    };

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
        imgPackerIdle = loadImage("/images/e_dormindo.png");
        Image imgTrainOriginal = loadImage("/images/trem.png");
        imgTrain = imgTrainOriginal.getScaledInstance(TREM_LARGURA_DESEJADA, TREM_ALTURA_DESEJADA, Image.SCALE_SMOOTH);
        imgPackerWorking = loadImage("/images/e_trabalhando.png");
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

        // 3. Desenhar os Empacotadores
        if (packerStatusMap == null) return;

        g2d.setColor(Color.WHITE); // Cor do texto
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        for (Map.Entry<Integer, String> entry : packerStatusMap.entrySet()) {
            int id = entry.getKey();
            String status = entry.getValue();

            // Pega a posição (x,y) do empacotador (ex: ID 1 -> pos[0])
            Point pos;
            if (id - 1 < packerPositions.length) {
                pos = packerPositions[id - 1];
            } else {
                continue; // ID do empacotador não mapeado para uma posição
            }

            // Escolhe a imagem (Sprite) correta
            Image packerImage = imgPackerIdle;
            if (status.equals("Empacotando")) {
                packerImage = imgPackerWorking;
            }

            // Desenha a imagem do empacotador
            g2d.drawImage(packerImage, pos.x, pos.y, this);

            // Desenha o status dele logo abaixo
            g2d.drawString(String.format("[%d] %s", id, status), pos.x, pos.y + packerImage.getHeight(null) + 15);
        }

        drawStatusHUD(g2d);
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
        g2d.fillRect(0, 0, getWidth(), hudHeight); // Desenha a barra no topo

        // --- 3. Desenhar Texto ---
        g2d.setColor(Color.WHITE); // Texto branco

        int xPadding = 5; // Distância da borda esquerda
        int yPos = 25;     // Posição Y (vertical) inicial

        // Coluna 1: Status Geral
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(String.format("Armazém: %d / %d", caixasArmazem, this.capacidadeDoDeposito), xPadding, yPos);

        yPos += 25; // Próxima linha
        g2d.drawString(String.format("Carga no Trem: %d / %d", caixasTrem, this.capacidadeDoTrem), xPadding, yPos);

        yPos += 25; // Próxima linha
        g2d.drawString(String.format("Status Trem: %s", statusTrem), xPadding, yPos);

        // Coluna 2: Status Empacotadores
        int xPosPackers = 350; // Posição X da segunda coluna
        yPos = 25; // Reseta o Y para o topo

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("--- Empacotadores ---", xPosPackers, yPos);

        // Fonte menor para a lista
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

        if (packerStatusMap != null) {
            for (Map.Entry<Integer, String> entry : packerStatusMap.entrySet()) {
                yPos += 20; // Próxima linha
                if (yPos >= hudHeight - 10) break; // Para de desenhar se sair do HUD

                String status = entry.getValue();
                g2d.drawString(String.format("Empacotador [%d]: %s", entry.getKey(), status), xPosPackers, yPos);
            }
        }
    }
}
