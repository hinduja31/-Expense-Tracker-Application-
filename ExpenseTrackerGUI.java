import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class ExpenseTrackerGUI extends JFrame {
    private ArrayList<String[]> expenses = new ArrayList<>();
    private DefaultTableModel model;
    private JTable table;
    private JTextField nameField, amountField, categoryField, searchField;
    private JLabel dashboardLabel;
    private int expenseCounter = 1001;
    private final String FILE_NAME = "expenses.txt";

    public ExpenseTrackerGUI() {
        loginPage();
    }

    private void loginPage() {
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();

        Object[] msg = {"Username:", user, "Password:", pass};

        int option = JOptionPane.showConfirmDialog(null, msg, "Login", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            if (user.getText().equals("admin") && String.valueOf(pass.getPassword()).equals("1234")) {
                buildGUI();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Login");
                System.exit(0);
            }
        } else System.exit(0);
    }

    private void buildGUI() {
        setTitle("Expense Tracker");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        dashboardLabel = new JLabel("Dashboard Summary");
        add(dashboardLabel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID","Name","Amount","Category"},0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(2,5));
        nameField = new JTextField();
        amountField = new JTextField();
        categoryField = new JTextField();
        searchField = new JTextField();

        form.add(new JLabel("Name"));
        form.add(new JLabel("Amount"));
        form.add(new JLabel("Category"));
        form.add(new JLabel("Search"));
        form.add(new JLabel(""));

        form.add(nameField);
        form.add(amountField);
        form.add(categoryField);
        form.add(searchField);

        JButton searchBtn = new JButton("Search");
        form.add(searchBtn);

        add(form, BorderLayout.SOUTH);

        JPanel buttons = new JPanel(new GridLayout(3,5));

        JButton addBtn=new JButton("Add Expense");
        JButton updateBtn=new JButton("Update");
        JButton deleteBtn=new JButton("Delete");
        JButton sortBtn=new JButton("Sort");
        JButton filterBtn=new JButton("Filter");
        JButton monthlyBtn=new JButton("Monthly Report");
        JButton categoryReportBtn=new JButton("Category Report");
        JButton recentBtn=new JButton("Recent");
        JButton topBtn=new JButton("Top Category");
        JButton savingsBtn=new JButton("Savings");
        JButton budgetBtn=new JButton("Budget Alert");
        JButton exportBtn=new JButton("Export");
        JButton categoryBtn=new JButton("Categories");

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(sortBtn);
        buttons.add(filterBtn);
        buttons.add(monthlyBtn);
        buttons.add(categoryReportBtn);
        buttons.add(recentBtn);
        buttons.add(topBtn);
        buttons.add(savingsBtn);
        buttons.add(budgetBtn);
        buttons.add(exportBtn);
        buttons.add(categoryBtn);

        add(buttons, BorderLayout.EAST);

        addBtn.addActionListener(e->addExpense());
        updateBtn.addActionListener(e->updateExpense());
        deleteBtn.addActionListener(e->deleteExpense());
        searchBtn.addActionListener(e->searchExpense());
        sortBtn.addActionListener(e->sortExpense());
        filterBtn.addActionListener(e->filterExpense());
        monthlyBtn.addActionListener(e->monthlyReport());
        categoryReportBtn.addActionListener(e->categoryReport());
        recentBtn.addActionListener(e->recentTransactions());
        topBtn.addActionListener(e->topCategory());
        savingsBtn.addActionListener(e->savingsCalculation());
        budgetBtn.addActionListener(e->budgetAlert());
        exportBtn.addActionListener(e->exportReport());
        categoryBtn.addActionListener(e->categoryManagement());

        loadData();
        updateDashboard();

        setVisible(true);
    }

    private String nextId() { return "EXP" + (expenseCounter++); }

    private void addExpense() {
        try {
            String[] row = {nextId(), nameField.getText(),
                    amountField.getText(), categoryField.getText()};
            expenses.add(row);
            model.addRow(row);
            saveData();
            updateDashboard();
        } catch(Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void updateExpense() {
        int r=table.getSelectedRow();
        if(r>=0){
            model.setValueAt(nameField.getText(),r,1);
            model.setValueAt(amountField.getText(),r,2);
            model.setValueAt(categoryField.getText(),r,3);
            saveData();
        }
    }

    private void deleteExpense() {
        int r=table.getSelectedRow();
        if(r>=0) model.removeRow(r);
        saveData();
    }

    private void searchExpense() {
        String key=searchField.getText().toLowerCase();
        for(int i=0;i<model.getRowCount();i++){
            if(model.getValueAt(i,1).toString().toLowerCase().contains(key)){
                table.setRowSelectionInterval(i,i);
                return;
            }
        }
    }

    private void filterExpense() {
        String cat=JOptionPane.showInputDialog("Category");
        for(int i=0;i<model.getRowCount();i++){
            if(model.getValueAt(i,3).equals(cat)){
                table.setRowSelectionInterval(i,i);
                break;
            }
        }
    }

    private void sortExpense() {
        expenses.sort(Comparator.comparing(a->a[1]));
        refreshTable();
    }

    private void monthlyReport() {
        JOptionPane.showMessageDialog(this,"Monthly Expenses: "+totalExpense());
    }

    private void categoryReport() {
        HashMap<String,Double> map=new HashMap<>();
        for(int i=0;i<model.getRowCount();i++){
            String c=model.getValueAt(i,3).toString();
            double a=Double.parseDouble(model.getValueAt(i,2).toString());
            map.put(c,map.getOrDefault(c,0.0)+a);
        }
        JOptionPane.showMessageDialog(this,map.toString());
    }

    private void recentTransactions() {
        int rows=model.getRowCount();
        if(rows>0) JOptionPane.showMessageDialog(this,model.getValueAt(rows-1,1));
    }

    private void topCategory() {
        categoryReport();
    }

    private void savingsCalculation() {
        double budget=10000;
        JOptionPane.showMessageDialog(this,"Savings: "+(budget-totalExpense()));
    }

    private void budgetAlert() {
        double budget=10000;
        double used=(totalExpense()/budget)*100;
        JOptionPane.showMessageDialog(this,"Budget Used: "+used+"%");
    }

    private void exportReport() {
        try(FileWriter fw=new FileWriter("report.txt")){
            fw.write("Total Expense: "+totalExpense());
            JOptionPane.showMessageDialog(this,"Exported");
        }catch(Exception e){}
    }

    private void categoryManagement() {
        JOptionPane.showMessageDialog(this,"Manage categories through Category field.");
    }

    private double totalExpense(){
        double t=0;
        for(int i=0;i<model.getRowCount();i++)
            t+=Double.parseDouble(model.getValueAt(i,2).toString());
        return t;
    }

    private void updateDashboard(){
        dashboardLabel.setText("Dashboard Summary | Total Expense: "+totalExpense());
    }

    private void refreshTable(){
        model.setRowCount(0);
        for(String[] r:expenses) model.addRow(r);
    }

    private void saveData(){
        try(FileWriter fw=new FileWriter(FILE_NAME)){
            for(int i=0;i<model.getRowCount();i++){
                fw.write(model.getValueAt(i,0)+","+model.getValueAt(i,1)+","+model.getValueAt(i,2)+","+model.getValueAt(i,3)+"\n");
            }
        }catch(Exception e){}
    }

    private void loadData(){
        try(BufferedReader br=new BufferedReader(new FileReader(FILE_NAME))){
            String line;
            while((line=br.readLine())!=null){
                String[] p=line.split(",");
                model.addRow(p);
                expenses.add(p);
            }
        }catch(Exception e){}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerGUI::new);
    }
}