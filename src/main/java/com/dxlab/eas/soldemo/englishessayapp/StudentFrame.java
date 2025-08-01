package com.dxlab.eas.soldemo.englishessayapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StudentFrame extends JFrame {
    private String studentId;
    private JTextArea essayArea;
    public DefaultTableModel topicTableModel;
    public DefaultTableModel draftTableModel;
    public DefaultTableModel submittedTableModel;
    public String selectedTopicId;
    public JTable topicTable;
    private final DialogManager dialogManager;

    public StudentFrame(String studentId) {
        this(studentId, new DefaultDialogManager());
    }

    public StudentFrame(String studentid, DialogManager dm) {
        this.studentId = studentid;
        this.dialogManager = dm;
        setTitle("Student Panel - ID: " + studentId);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Toolbar with Logout
        JToolBar toolBar = new JToolBar();
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        toolBar.add(logoutButton);
        add(toolBar, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Select Topic Tab (Main View)
        JPanel topicPanel = new JPanel(new BorderLayout());
        topicTableModel = new DefaultTableModel(new String[] { "Topic ID", "Description" }, 0);
        topicTable = new JTable(topicTableModel);
        topicTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewTopic();
                }
            }
        });
        JButton selectTopicButton = new JButton("Select Topic");
        JButton cancelTopicButton = new JButton("Cancel Selection");
        selectTopicButton.addActionListener(e -> selectTopic());
        cancelTopicButton.addActionListener(e -> selectedTopicId = null);
        JPanel topicButtonPanel = new JPanel();
        topicButtonPanel.add(selectTopicButton);
        topicButtonPanel.add(cancelTopicButton);
        topicPanel.add(new JScrollPane(topicTable), BorderLayout.CENTER);
        topicPanel.add(topicButtonPanel, BorderLayout.SOUTH);
        loadTopics();

        // Write Essay Tab
        JPanel writePanel = new JPanel(new BorderLayout());
        essayArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(essayArea);
        JPanel buttonPanel = new JPanel();
        JButton saveDraftButton = new JButton("Save Draft");
        JButton submitButton = new JButton("Submit Essay");
        saveDraftButton.addActionListener(e -> saveDraft());
        submitButton.addActionListener(e -> submitEssay());
        buttonPanel.add(saveDraftButton);
        buttonPanel.add(submitButton);
        writePanel.add(scrollPane, BorderLayout.CENTER);
        writePanel.add(buttonPanel, BorderLayout.SOUTH);

        // View Drafts Tab (Main View)
        JPanel draftsPanel = new JPanel(new BorderLayout());
        draftTableModel = new DefaultTableModel(new String[] { "Draft ID", "Topic ID", "Content Preview" }, 0);
        JTable draftTable = new JTable(draftTableModel);
        draftTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewDraft(draftTable);
                }
            }
        });
        JPanel draftButtonPanel = new JPanel();
        JButton viewDraftButton = new JButton("View Draft");
        JButton editDraftButton = new JButton("Edit Draft");
        JButton deleteDraftButton = new JButton("Delete Draft");
        viewDraftButton.addActionListener(e -> viewDraft(draftTable));
        editDraftButton.addActionListener(e -> editDraft(draftTable));
        deleteDraftButton.addActionListener(e -> deleteDraft(draftTable));
        draftButtonPanel.add(viewDraftButton);
        draftButtonPanel.add(editDraftButton);
        draftButtonPanel.add(deleteDraftButton);
        draftsPanel.add(new JScrollPane(draftTable), BorderLayout.CENTER);
        draftsPanel.add(draftButtonPanel, BorderLayout.SOUTH);
        loadDrafts();

        // View Submitted Essays Tab (Main View)
        JPanel submittedPanel = new JPanel(new BorderLayout());
        submittedTableModel = new DefaultTableModel(new String[] { "Essay ID", "Topic ID", "Grade Summary" }, 0);
        JTable submittedTable = new JTable(submittedTableModel);
        submittedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewResult(submittedTable);
                }
            }
        });
        JButton viewResultButton = new JButton("View Essay and Result");
        viewResultButton.addActionListener(e -> viewResult(submittedTable));
        submittedPanel.add(new JScrollPane(submittedTable), BorderLayout.CENTER);
        submittedPanel.add(viewResultButton, BorderLayout.SOUTH);
        loadSubmitted();

        tabbedPane.addTab("Select Topic", topicPanel);
        tabbedPane.addTab("Write Essay", writePanel);
        tabbedPane.addTab("View Drafts", draftsPanel);
        tabbedPane.addTab("View Submitted", submittedPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadTopics() {
        topicTableModel.setRowCount(0);
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.TOPICS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 2);
                if (parts.length >= 2) {
                    topicTableModel.addRow(new Object[] { parts[0], parts[1] });
                }
            }
        } catch (IOException ex) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EnglishEssayApp.TOPICS_FILE))) {
                writer.write(
                        "T1 | Some people think that technology improves education. To what extent do you agree or disagree?");
                writer.newLine();
                writer.write("T2 | Discuss the advantages and disadvantages of living in a big city.");
                writer.newLine();
                writer.write("T3 | Should governments invest more in public transportation? Why or why not?");
                writer.newLine();
                topicTableModel
                        .addRow(new Object[] { "T1", "Some people think that technology improves education..." });
                topicTableModel.addRow(
                        new Object[] { "T2", "Discuss the advantages and disadvantages of living in a big city." });
                topicTableModel
                        .addRow(new Object[] { "T3", "Should governments invest more in public transportation..." });
            } catch (IOException e) {
                dialogManager.showMessage("Error initializing topics: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void selectTopic() {
        int row = topicTable.getSelectedRow();
        if (row == -1) {
            dialogManager.showMessage("Please select a topic!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectedTopicId = (String) topicTableModel.getValueAt(row, 0);
        dialogManager.showMessage("Selected topic: " + topicTableModel.getValueAt(row, 1), "Topic Selected",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void viewTopic() {
        int row = topicTable.getSelectedRow();
        if (row == -1) {
            dialogManager.showMessage("Please select a topic!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String topicId = (String) topicTableModel.getValueAt(row, 0);
        String description = (String) topicTableModel.getValueAt(row, 1);
        dialogManager.showMessage(description, "Topic: " + topicId, JOptionPane.PLAIN_MESSAGE);
    }

    private void saveDraft() {
        if (selectedTopicId == null) {
            dialogManager.showMessage("Please select a topic first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String content = essayArea.getText().trim();
        if (!content.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EnglishEssayApp.DRAFTS_FILE, true))) {
                String draftId = studentId + "_" + System.currentTimeMillis();
                writer.write(studentId + " | " + draftId + " | " + selectedTopicId + " | " + content);
                writer.newLine();
                draftTableModel.addRow(new Object[] { draftId, selectedTopicId,
                        content.length() > 50 ? content.substring(0, 50) + "..." : content });
                dialogManager.showMessage("Draft saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                essayArea.setText("");
                selectedTopicId = null;
            } catch (IOException ex) {
                dialogManager.showMessage("Error saving draft: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            dialogManager.showMessage("Please write something before saving!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void submitEssay() {
        if (selectedTopicId == null) {
            dialogManager.showMessage("Please select a topic first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String content = essayArea.getText().trim();
        if (!content.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EnglishEssayApp.SUBMITTED_FILE, true))) {
                String essayId = studentId + "_" + System.currentTimeMillis();
                writer.write(studentId + " | " + essayId + " | " + selectedTopicId + " | " + content);
                writer.newLine();
                submittedTableModel.addRow(new Object[] { essayId, selectedTopicId, "Not graded" });
                dialogManager.showMessage("Essay submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                essayArea.setText("");
                selectedTopicId = null;
            } catch (IOException ex) {
                dialogManager.showMessage("Error submitting essay: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            dialogManager.showMessage("Please write something before submitting!", "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void loadDrafts() {
        draftTableModel.setRowCount(0);
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.DRAFTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 4);
                if (parts.length >= 4 && parts[0].equals(studentId)) {
                    String preview = parts[3].length() > 50 ? parts[3].substring(0, 50) + "..." : parts[3];
                    draftTableModel.addRow(new Object[] { parts[1], parts[2], preview });
                }
            }
        } catch (IOException ex) {
            // File may not exist yet
        }
    }

    public void loadSubmitted() {
        submittedTableModel.setRowCount(0);
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.SUBMITTED_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 4);
                if (parts.length >= 4 && parts[0].equals(studentId)) {
                    submittedTableModel.addRow(new Object[] { parts[1], parts[2], getGradeSummary(parts[1]) });
                }
            }
        } catch (IOException ex) {
            // File may not exist yet
        }
    }

    public String getGradeSummary(String essayId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.GRADED_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 8);
                if (parts.length >= 7 && parts[1].equals(essayId)) {
                    return String.format("Task: %s, Coherence: %s, Lexical: %s, Grammar: %s",
                            parts[3], parts[4], parts[5], parts[6]);
                }
            }
        } catch (IOException ex) {
            // File may not exist yet
        }
        return "Not graded";
    }

    private void viewDraft(JTable draftTable) {
        int row = draftTable.getSelectedRow();
        if (row == -1) {
            dialogManager.showMessage("Please select a draft!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String draftId = (String) draftTableModel.getValueAt(row, 0);
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.DRAFTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 4);
                if (parts.length >= 4 && parts[1].equals(draftId)) {
                    dialogManager.showMessage(
                            "Topic: " + getTopicContent(parts[2]) + "\n\nContent:\n" + parts[3],
                            "Draft: " + draftId, JOptionPane.PLAIN_MESSAGE);
                    return;
                }
            }
        } catch (IOException ex) {
            dialogManager.showMessage("Error loading draft: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editDraft(JTable draftTable) {
        int row = draftTable.getSelectedRow();
        if (row == -1) {
            dialogManager.showMessage("Please select a draft!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String draftId = (String) draftTableModel.getValueAt(row, 0);
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.DRAFTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 4);
                if (parts.length >= 4 && parts[1].equals(draftId)) {
                    selectedTopicId = parts[2];
                    essayArea.setText(parts[3]);
                    dialogManager.showMessage(
                            "Draft loaded into editor. Topic: " + getTopicContent(parts[2]), "Draft Loaded",
                            JOptionPane.INFORMATION_MESSAGE);
                    updateDraft(draftId);
                    return;
                }
            }
        } catch (IOException ex) {
            dialogManager.showMessage("Error loading draft: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDraft(JTable draftTable) {
        int row = draftTable.getSelectedRow();
        if (row == -1) {
            dialogManager.showMessage("Please select a draft!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String draftId = (String) draftTableModel.getValueAt(row, 0);
        int confirm = dialogManager.showConfirmDialog("Delete this draft?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.DRAFTS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" \\| ", 4);
                    if (parts.length >= 4 && !parts[1].equals(draftId)) {
                        lines.add(line);
                    }
                }
            } catch (IOException ex) {
                dialogManager.showMessage("Error reading drafts: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EnglishEssayApp.DRAFTS_FILE))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                draftTableModel.removeRow(row);
                dialogManager.showMessage("Draft deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                dialogManager.showMessage("Error deleting draft: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateDraft(String draftId) {
        int result = dialogManager.showConfirmDialog("Save changes to this draft?", "Edit Draft",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newContent = essayArea.getText().trim();
            if (newContent.isEmpty()) {
                dialogManager.showMessage("Draft content cannot be empty!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.DRAFTS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" \\| ", 4);
                    if (parts.length >= 4 && parts[1].equals(draftId)) {
                        lines.add(studentId + " | " + draftId + " | " + selectedTopicId + " | " + newContent);
                    } else {
                        lines.add(line);
                    }
                }
            } catch (IOException ex) {
                dialogManager.showMessage("Error reading drafts: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EnglishEssayApp.DRAFTS_FILE))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                dialogManager.showMessage("Draft updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                essayArea.setText("");
                selectedTopicId = null;
                loadDrafts();
            } catch (IOException ex) {
                dialogManager.showMessage("Error updating draft: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getTopicContent(String topicId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.TOPICS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 2);
                if (parts.length >= 2 && parts[0].equals(topicId)) {
                    return parts[1];
                }
            }
        } catch (IOException ex) {
            // File may not exist
        }
        return "Unknown topic";
    }

    private void viewResult(JTable submittedTable) {
        int row = submittedTable.getSelectedRow();
        if (row == -1) {
            dialogManager.showMessage("Please select an essay!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String essayId = (String) submittedTableModel.getValueAt(row, 0);
        String topicId = (String) submittedTableModel.getValueAt(row, 1);
        String content = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.SUBMITTED_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 4);
                if (parts.length >= 4 && parts[1].equals(essayId)) {
                    content = parts[3];
                    break;
                }
            }
        } catch (IOException ex) {
            dialogManager.showMessage("Error loading essay: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextArea contentArea = new JTextArea("Topic: " + getTopicContent(topicId) + "\n\nContent:\n" + content);
        contentArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        try (BufferedReader reader = new BufferedReader(new FileReader(EnglishEssayApp.GRADED_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ", 8);
                if (parts.length >= 7 && parts[1].equals(essayId)) {
                    contentArea.append(String.format("\n\nResult:\nTask Achievement: %s\nCoherence and Cohesion: %s\n" +
                            "Lexical Resource: %s\nGrammatical Range and Accuracy: %s\nComments: %s",
                            parts[3], parts[4], parts[5], parts[6], parts[7]));
                    dialogManager.showMessage(scrollPane.toString(), "Essay and Result: " + essayId,
                            JOptionPane.PLAIN_MESSAGE);
                    return;
                }
            }
            dialogManager.showMessage(scrollPane.toString(), "Essay: " + essayId + " (Not graded)",
                    JOptionPane.PLAIN_MESSAGE);
        } catch (IOException ex) {
            dialogManager.showMessage("Error loading result: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
