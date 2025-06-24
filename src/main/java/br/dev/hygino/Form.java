package br.dev.hygino;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Form {

    private boolean classeValida = false;
    private final Set<String> listaClasses = new HashSet<>();

    private JFrame janela;
    private JLabel lbCaminhoArquivo, lbBasePackage;
    private JButton btnBuscarArquivo, btnAdicionaEntidade, btnGeraClasses;
    private JTextArea txtResultado;
    private JTextField txtNomeClasse;

    public Form() {
        criarJanela();
    }

    private void criarJanela() {
        janela = new JFrame("Gerador de Classes");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setSize(640, 480);
        janela.setLocationRelativeTo(null);

        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BorderLayout());

        painelPrincipal.add(criarPainelTopo(), BorderLayout.NORTH);
        painelPrincipal.add(criarPainelLog(), BorderLayout.CENTER);

        janela.add(painelPrincipal);
        janela.setVisible(true);
    }

    private JPanel criarPainelTopo() {
        JPanel painelTopo = new JPanel();
        painelTopo.setLayout(new BoxLayout(painelTopo, BoxLayout.Y_AXIS));

        lbCaminhoArquivo = new JLabel("Caminho da classe principal");
        lbBasePackage = new JLabel("Pacote base: ");

        JPanel linhaBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtNomeClasse = new JTextField(20);
        btnBuscarArquivo = new JButton("Buscar");
        btnAdicionaEntidade = new JButton("Adicionar");
        btnGeraClasses = new JButton("Gerar");

        txtNomeClasse.setEnabled(false);
        btnAdicionaEntidade.setEnabled(false);
        btnGeraClasses.setEnabled(false);

        linhaBotoes.add(btnBuscarArquivo);
        linhaBotoes.add(txtNomeClasse);
        linhaBotoes.add(btnAdicionaEntidade);
        linhaBotoes.add(btnGeraClasses);

        painelTopo.add(lbCaminhoArquivo);
        painelTopo.add(lbBasePackage);
        painelTopo.add(linhaBotoes);

        btnBuscarArquivo.addActionListener(this::buscarArquivo);

        return painelTopo;
    }

    private JScrollPane criarPainelLog() {
        txtResultado = new JTextArea("Log da aplicação", 15, 50);
        txtResultado.setEditable(false);
        txtResultado.setMargin(new Insets(5, 5, 5, 5));
        return new JScrollPane(txtResultado);
    }

    private void buscarArquivo(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos Java", "java"));

        int resultado = fileChooser.showOpenDialog(janela);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            processarArquivo(fileChooser.getSelectedFile());
        }

        btnAdicionaEntidade.setEnabled(classeValida);
        btnGeraClasses.setEnabled(classeValida);
        txtNomeClasse.setEnabled(classeValida);
    }

    private void processarArquivo(File arquivo) {
        lbCaminhoArquivo.setText("Arquivo selecionado: " + arquivo.getAbsolutePath());

        try {
            List<String> linhas = Files.readAllLines(arquivo.toPath());

            boolean temMain = linhas.stream().anyMatch(l -> l.contains("public static void main"));
            boolean temSpring = linhas.stream().anyMatch(l -> l.contains("@SpringBootApplication"));
            String basePackage = linhas.stream()
                    .filter(l -> l.startsWith("package"))
                    .findFirst()
                    .map(l -> l.replace("package", "").replace(";", "").trim())
                    .orElse("Não encontrado");

            // Atualiza label
            lbBasePackage.setText("Pacote base: " + basePackage);

            // Define se a classe é válida
            classeValida = temMain && temSpring && !basePackage.equals("Não encontrado");

            // Log
            StringBuilder resultado = new StringBuilder("\n--- Análise do Arquivo ---\n");
            resultado.append("Pacote base: ").append(basePackage).append("\n");
            resultado.append("Contém método main(): ").append(temMain ? "Sim" : "Não").append("\n");
            resultado.append("Contém @SpringBootApplication: ").append(temSpring ? "Sim" : "Não").append("\n");
            resultado.append("Classe válida para geração: ").append(classeValida ? "Sim" : "Não").append("\n");

            txtResultado.append(resultado.toString());

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(janela, "Erro ao ler o arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

}
