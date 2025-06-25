package br.dev.hygino;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Form {

    private boolean classeValida = false;
    private final Set<String> listaClasses = new HashSet<>();
    private String basePackage, rootPackageDirectory;

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
        btnAdicionaEntidade.addActionListener(this::addEntityClass);
        btnGeraClasses.addActionListener(this::generateCrudClasses);

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

        rootPackageDirectory = arquivo.getParent();
        txtResultado.append('\n' + rootPackageDirectory);

        try {
            List<String> linhas = Files.readAllLines(arquivo.toPath());

            boolean temMain = linhas.stream().anyMatch(l -> l.contains("public static void main"));
            boolean temSpring = linhas.stream().anyMatch(l -> l.contains("@SpringBootApplication"));
            basePackage = linhas.stream()
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
            JOptionPane.showMessageDialog(janela, "Erro ao ler o arquivo: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEntityClass(ActionEvent event) {
        String className = txtNomeClasse.getText().trim();

        if (isNotEmptyAndStartsWithLetter(className)) {
            listaClasses.add(className);
            txtResultado.append("Entidade adicionada: " + className + '\n');
        } else {
            JOptionPane.showMessageDialog(null, "Ocorreu um erro!", "Entidade Inválida", JOptionPane.ERROR_MESSAGE);
        }

        txtNomeClasse.setText("");
        txtNomeClasse.requestFocus();
    }

    private boolean isNotEmptyAndStartsWithLetter(String str) {
        return str != null && !str.isEmpty() && Character.isLetter(str.charAt(0));
    }

    private void generateEntities() {

        listaClasses.forEach(entity -> {
            // Caminho do arquivo .java
            File arquivo = new File(rootPackageDirectory + "/entities/" + entity + ".java");

            // Garante que o diretório "entities" exista
            File pastaEntities = arquivo.getParentFile();
            if (!pastaEntities.exists()) {
                pastaEntities.mkdirs(); // Cria os diretórios se não existirem
            }

            // Conteúdo da classe
            StringBuilder sb = new StringBuilder("package " + basePackage + ".entities;\n\n");
            sb.append("""
                    import jakarta.persistence.Column;
                    import jakarta.persistence.Entity;
                    import jakarta.persistence.GeneratedValue;
                    import jakarta.persistence.GenerationType;
                    import jakarta.persistence.Id;
                    import jakarta.persistence.Table;

                    """);
            sb.append("@Entity\n@Table\n");
            sb.append("public class ").append(entity).append(" {\n\n");
            sb.append("""
                    @Id
                    @GeneratedValue(strategy = GenerationType.IDENTITY)
                    @Column(nullable = false)
                    private Long id;

                    """);
            sb.append("}\n");

            // Escrita no arquivo
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace(); // ou exiba em JOptionPane, se desejar
            }
        });
    }

    private void generateRepositories() {

        listaClasses.forEach(entity -> {
            // Caminho do arquivo .java
            File arquivo = new File(rootPackageDirectory + "/repositories/" + entity + "Repository.java");

            // Garante que o diretório "repositories" exista
            File pastaRepositories = arquivo.getParentFile();
            if (!pastaRepositories.exists()) {
                pastaRepositories.mkdirs(); // Cria os diretórios se não existirem
            }

            // Conteúdo da interface
            StringBuilder sb = new StringBuilder("package " + basePackage + ".repositories;\n");
            sb.append("\nimport " + basePackage + ".entities." + entity + ";\n");
            sb.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
            sb.append("public interface " + entity + "Repository extends JpaRepository<" + entity + ",Long>{\n");
            sb.append("}\n");

            // Escrita no arquivo
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace(); // ou exiba em JOptionPane, se desejar
            }
        });
    }

    private void generateServices() {

        listaClasses.forEach(entity -> {
            // Caminho do arquivo .java
            File arquivo = new File(rootPackageDirectory + "/services/" + entity + "Service.java");

            // Garante que o diretório "services" exista
            File pastaServices = arquivo.getParentFile();
            if (!pastaServices.exists()) {
                pastaServices.mkdirs(); // Cria os diretórios se não existirem
            }

            // Conteúdo da interface
            StringBuilder sb = new StringBuilder("package " + basePackage + ".repositories;\n");
            sb.append("\nimport " + basePackage + ".repositories." + entity + "Repository;\n");

            sb.append("import org.springframework.stereotype.Service;\n");
            sb.append("import org.springframework.transaction.annotation.Transactional;\n\n");
            sb.append("@Service\n");
            sb.append("public class " + entity + "Service{\n");
            final String repository = entity + "Repository";
            sb.append("private final " + repository + " repository;\n");
            sb.append("public " + entity + "Service(" + repository + " repository){\n");
            sb.append("this.repository = repository;\n}\n");
            sb.append("}\n");

            // Escrita no arquivo
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace(); // ou exiba em JOptionPane, se desejar
            }
        });
    }

    private void generateCrudClasses(ActionEvent event) {
        if (!listaClasses.isEmpty()) {
            generateEntities();// .forEach(txtResultado::append);
            generateRepositories();
            generateServices();
        }
    }
}
