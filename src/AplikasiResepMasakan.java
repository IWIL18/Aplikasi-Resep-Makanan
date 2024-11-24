import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

public class AplikasiResepMasakan extends javax.swing.JFrame {
    private DefaultTableModel tableModel;

    public AplikasiResepMasakan() {
        initComponents();
        // Inisialisasi model tabel dengan kolom tambahan "Tanggal"
        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Bahan", "Langkah", "Tanggal"}, 0);
        // Menghubungkan model ke JTable
        kontakTable.setModel(tableModel);

        kontakTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = kontakTable.getSelectedRow();
            if (selectedRow != -1) { // Pastikan ada baris yang dipilih
                // Ambil data dari baris yang dipilih
                String nama = (String) tableModel.getValueAt(selectedRow, 1); // Ambil nama dari kolom ke-1
                String bahan = (String) tableModel.getValueAt(selectedRow, 2); // Ambil bahan dari kolom ke-2
                String langkah = (String) tableModel.getValueAt(selectedRow, 3); // Ambil langkah dari kolom ke-3
                Object tanggalObj = tableModel.getValueAt(selectedRow, 4); // Ambil tanggal dari kolom ke-4
                
                String tanggal = "";
                if (tanggalObj != null) {
                    if (tanggalObj instanceof Date) {
                        // Format tanggal jika berupa Date
                        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");
                        tanggal = sdf.format((Date) tanggalObj);
                    } else {
                        // Asumsikan sudah berupa String
                        tanggal = tanggalObj.toString();
                    }
                }
                
                // Masukkan data ke dalam JTextField
                namaTextField.setText(nama);
                textbahan.setText(bahan);
                textlangkah.setText(langkah);
                tanggalbuat.setDate(java.sql.Date.valueOf(tanggal)); // Atur ke JDateChooser
            }
        });
    }
  


public class DatabaseHelper {
    private static final String DATABASE_NAME = "resep.sqlite";

    // Method untuk membuat koneksi database
    public Connection connect() {
        Connection conn = null;
        try {
            // Cek apakah database sudah ada
            File dbFile = new File(DATABASE_NAME);
            boolean databaseBaru = !dbFile.exists(); // Jika file tidak ada, berarti database baru

            // Inisialisasi koneksi ke database
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            System.out.println("Koneksi database berhasil.");

            // Jika database baru, buat tabel
            if (databaseBaru) {
                buatTabel(conn);
                System.out.println("Database baru dibuat dengan tabel default.");
            }
        } catch (Exception e) {
            System.out.println("Koneksi database gagal: " + e.getMessage());
        }
        return conn;
    }

    // Method untuk membuat tabel jika belum ada
    private void buatTabel(Connection conn) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS resep (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "nama TEXT NOT NULL, " +
                                "bahan TEXT NOT NULL, " +
                                "langkah TEXT NOT NULL, " +
                                "tanggal DATE NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Tabel 'resep' berhasil dibuat.");
        } catch (SQLException e) {
            System.out.println("Gagal membuat tabel: " + e.getMessage());
        }
    }
}



    private void updateListCariFromTable() {
        DefaultListModel<String> listModel = new DefaultListModel<>();

        DefaultTableModel tableModel = (DefaultTableModel) kontakTable.getModel();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nama = (String) tableModel.getValueAt(i, 1);  // Ambil judul dari kolom pertama
            listModel.addElement(nama);  // Tambahkan judul ke list
        }

        listResep.setModel(listModel);  // Set model ke listResep
    }

       public void tambahResep(String nama, String bahan, String langkah,String tanggal) {
      DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "INSERT INTO resep (nama, bahan, langkah, tanggal) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nama);      // Masukkan nama
        pstmt.setString(2, bahan);    // Masukkan bahan
        pstmt.setString(3, langkah);  // Masukkan langkah
        pstmt.setString(4, tanggal);    // Masukkan tanggal
        pstmt.executeUpdate();        // Eksekusi perintah SQL
        System.out.println("Data berhasil ditambahkan.");
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
}
    
// Setup listener untuk pemilihan di list pencarian
    private void setupListSelectionListener() {
    listResep.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            String selectedJudul = listResep.getSelectedValue();
            if (selectedJudul != null) {
                // Cari catatan di tabel berdasarkan judul
                DefaultTableModel model = (DefaultTableModel) kontakTable.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    String nama = (String) model.getValueAt(i, 1);
                    if (nama.equals(selectedJudul)) {
                        // Pilih baris yang sesuai di jTable1
                        kontakTable.setRowSelectionInterval(i, i);

                        // Set data ke form
                        String bahan = (String) model.getValueAt(i, 1);
                        String langkah = (String) model.getValueAt(i, 2);
                        String tanggal = (String) model.getValueAt(i, 3);
                        

                        namaTextField.setText(nama);
                        textbahan.setText(bahan);
                        textlangkah.setText(langkah);

                        try {
                            // Format tanggal dari string ke Date dan set ke komponen dateTanggal
                            java.util.Date dateValue = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(tanggal);
                            tanggalbuat.setDate(dateValue);
                        } catch (java.text.ParseException ex) {
                            ex.printStackTrace();
                        }

                        break;
                    }
                }
            }
        }
    });       
    }
    public List<String[]> getResepList() {
    List<String[]> kontakList = new ArrayList<>();
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "SELECT * FROM resep";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            String[] resep = new String[5];
            resep[0] = String.valueOf(rs.getInt("id")); // Ambil id
            resep[1] = rs.getString("nama");
            resep[2] = rs.getString("bahan");
            resep[3] = rs.getString("langkah"); // Perbaikan typo
            resep[4] = rs.getString("tanggal");
            kontakList.add(resep);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return kontakList;
}
    
    public void updateKontak(int id, String nama, String bahan, String langkah, String tanggal ) {
    DatabaseHelper dbHelper = new DatabaseHelper();
try (Connection conn = dbHelper.connect()) {
    // SQL untuk mengupdate data berdasarkan ID
    String sql = "UPDATE resep SET nama = ?, bahan = ?, langkah = ?, tanggal = ? WHERE id = ?";
    PreparedStatement pstmt = conn.prepareStatement(sql);

    // Ambil data dari form input
    pstmt.setString(1, nama);      // Masukkan nama
    pstmt.setString(2, bahan);    // Masukkan bahan
    pstmt.setString(3, langkah);  // Masukkan langkah
    pstmt.setString(4, tanggal);  // Masukkan tanggal dalam format yyyy-MM-dd
    pstmt.setInt(5, id);          // ID baris yang akan diubah

    int rowsUpdated = pstmt.executeUpdate(); // Eksekusi perintah SQL

    if (rowsUpdated > 0) {
        System.out.println("Data berhasil diubah.");
        JOptionPane.showMessageDialog(this, "Resep berhasil diubah!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
    } else {
        System.out.println("Tidak ada data yang diubah.");
        JOptionPane.showMessageDialog(this, "Resep dengan ID " + id + " tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
    }
} catch (SQLException e) {
    e.printStackTrace();
    JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mengubah data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
}

}
    
    public void hapusKontak(int id) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "DELETE FROM resep WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
   public void tampilkanResep(DefaultTableModel model) {
     model.setRowCount(0); // Hapus data lama
    List<String[]> kontakList = getResepList(); // Ambil data dari database

    for (String[] kontak : kontakList) {
        model.addRow(kontak); // Tambahkan data ke JTable
    }
}
   
   public List<String[]> cariResep(String keyword) {
    List<String[]> kontakList = new ArrayList<>();
    DatabaseHelper dbHelper = new DatabaseHelper();
    
    try (Connection conn = dbHelper.connect()) {
        String sql = "SELECT * FROM resep WHERE nama LIKE ? OR bahan LIKE ? OR langkah LIKE ? OR tanggal LIKE ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "%" + keyword + "%");
        pstmt.setString(2, "%" + keyword + "%");
        pstmt.setString(3, "%" + keyword + "%");
        pstmt.setString(4, "%" + keyword + "%");
        
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            String[] resepp = new String[5];
            resepp[0] = String.valueOf(rs.getInt("id"));
            resepp[1] = rs.getString("nama");
            resepp[2] = rs.getString("bahan");
            resepp[3] = rs.getString("langkah");
            resepp[4] = rs.getString("tanggal");
            kontakList.add(resepp);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return kontakList;
}
   
   public void tampilkanKontakBerdasarkanKategori(String kategori) {
    tableModel.setRowCount(0); // Menghapus semua baris lama di tabel
    if (kategori == null || tableModel == null) {
        System.out.println("Kategori atau tableModel tidak boleh null");
        return;
    }

    List<String[]> resepList = getResepList(); // Dapatkan daftar semua kontak

    // Filter kontak berdasarkan kategori
    for (String[] resep : resepList) {
        if (resep[3].equals(kategori)) { // Asumsi kontak[3] adalah kolom kategori
            tableModel.addRow(resep); // Tambahkan kontak yang sesuai ke tabel
        }
    }
}
   
  
    public void eksporKeCSV(String filePath) {
        try (PrintWriter writer = new PrintWriter(new File("Resep.csv"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("ID,Name,Bahan,Langkah,Tanggal\n");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                sb.append(tableModel.getValueAt(i, 0)).append(",");
                sb.append(tableModel.getValueAt(i, 1)).append(",");
                sb.append(tableModel.getValueAt(i, 2)).append(",");
                sb.append(tableModel.getValueAt(i, 3)).append(",");
                sb.append(tableModel.getValueAt(i, 4)).append(",");
                sb.append(tableModel.getValueAt(i, 4)).append("\n");
            }
            writer.write(sb.toString());
            JOptionPane.showMessageDialog(this, "Resep Berhasil Diexport Ke CSV!");
        } catch (Exception e) {
            e.printStackTrace();
    }
    
}
   
   public void imporDariCSV(String filePath) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
         Connection conn = dbHelper.connect()) {
         
        String line;
        
        // Lewati header
        reader.readLine();
        
        // Iterasi setiap baris
        while ((line = reader.readLine()) != null) {
            String[] data = line.split(",");
            
            if (data.length == 5) { // Pastikan ada 4 kolom data
                String id = data[0];
                String nama = data[1];
                String bahan = data[2];
                String langkah = data[3];
                String tanggal = data[4];

                
                // Tambahkan data ke database
                String sql = "INSERT INTO resep (id, nama, bahan, langkah) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.setString(2, nama);
                pstmt.setString(3, bahan);
                pstmt.setString(4, langkah);
                pstmt.setString(5, tanggal);
                pstmt.executeUpdate();
            }
        }
        
        JOptionPane.showMessageDialog(this, "Data berhasil diimpor dari " + filePath, "Impor Berhasil", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (IOException | SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mengimpor data", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

   
   

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        namaTextField = new javax.swing.JTextField();
        tambahButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        hapusButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        textbahan = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        textlangkah = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        tanggalbuat = new com.toedter.calendar.JDateChooser();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        kontakTable = new javax.swing.JTable();
        eksporButton = new javax.swing.JButton();
        imporButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        listResep = new javax.swing.JList<>();
        jLabel5 = new javax.swing.JLabel();
        caritext = new javax.swing.JTextField();
        cariButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Aplikasi Resep Masakan", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        jLabel1.setText("Nama Resep");

        jLabel2.setText("Tanggal Buat");

        jLabel3.setText("Langkah-langkah");

        namaTextField.setBackground(new java.awt.Color(204, 204, 204));

        tambahButton.setBackground(new java.awt.Color(204, 255, 204));
        tambahButton.setText("Tambah");
        tambahButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tambahButtonActionPerformed(evt);
            }
        });

        editButton.setBackground(new java.awt.Color(204, 204, 255));
        editButton.setText("Ubah");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        hapusButton.setBackground(new java.awt.Color(255, 204, 204));
        hapusButton.setText("Hapus");
        hapusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapusButtonActionPerformed(evt);
            }
        });

        textbahan.setBackground(new java.awt.Color(204, 204, 204));
        textbahan.setColumns(20);
        textbahan.setRows(5);
        jScrollPane2.setViewportView(textbahan);

        textlangkah.setBackground(new java.awt.Color(204, 204, 204));
        textlangkah.setColumns(20);
        textlangkah.setRows(5);
        jScrollPane3.setViewportView(textlangkah);

        jLabel4.setText("Bahan-bahan");

        tanggalbuat.setBackground(new java.awt.Color(204, 204, 204));

        jPanel2.setBackground(new java.awt.Color(204, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Daftar Resep"));

        kontakTable.setBackground(new java.awt.Color(204, 255, 255));
        kontakTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Nama Resep", "Bahan-bahan", "Langkah-langkah", "Tanggal Buat"
            }
        ));
        jScrollPane1.setViewportView(kontakTable);

        eksporButton.setBackground(new java.awt.Color(204, 255, 204));
        eksporButton.setText("Exspor Data");
        eksporButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eksporButtonActionPerformed(evt);
            }
        });

        imporButton.setBackground(new java.awt.Color(204, 204, 255));
        imporButton.setText("Impor Data");
        imporButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imporButtonActionPerformed(evt);
            }
        });

        listResep.setBackground(new java.awt.Color(204, 204, 204));
        jScrollPane4.setViewportView(listResep);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("Data Resep");

        caritext.setBackground(new java.awt.Color(204, 204, 204));

        cariButton.setBackground(new java.awt.Color(255, 255, 204));
        cariButton.setText("Cari");
        cariButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cariButtonActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("List Resep");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(eksporButton)
                        .addGap(18, 18, 18)
                        .addComponent(imporButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 536, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(84, 84, 84)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(cariButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(caritext, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel6)
                                .addGap(26, 26, 26)))))
                .addContainerGap(114, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(caritext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cariButton)
                        .addComponent(jLabel6)))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(imporButton)
                    .addComponent(eksporButton))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel4))
                                .addGap(41, 41, 41))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(52, 52, 52)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(tanggalbuat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(namaTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
                        .addGap(35, 35, 35)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(tambahButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(hapusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(namaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane2))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2)
                            .addComponent(tanggalbuat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hapusButton)
                            .addComponent(editButton)
                            .addComponent(tambahButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tambahButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tambahButtonActionPerformed
        // TODO add your handling code here:
    String nama = namaTextField.getText();
    String bahan = textbahan.getText();
    String langkah = textlangkah.getText();
    java.util.Date date = tanggalbuat.getDate();
    
    // Validasi input
    if (nama.isEmpty() || bahan.isEmpty() || langkah.isEmpty() || date == null) {
        JOptionPane.showMessageDialog(this, "Semua kolom harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Konversi java.util.Date ke java.sql.Date
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(sqlDate);
    
    
    // Panggil fungsi tambahKontak
    tambahResep(nama, bahan, langkah, tanggal);

    
    // Refresh JTable
    tampilkanResep(tableModel);
    updateListCariFromTable();
    
    JOptionPane.showMessageDialog(this, "Resep berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
    
    }//GEN-LAST:event_tambahButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // TODO add your handling code here:
   int selectedRow = kontakTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih baris yang ingin diubah!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String idStr = tableModel.getValueAt(selectedRow, 0).toString();
    int id = Integer.parseInt(idStr);

    String nama = namaTextField.getText();
    String bahan = textbahan.getText();
    String langkah = textlangkah.getText();
    java.util.Date date = tanggalbuat.getDate();

    if (nama.isEmpty() || bahan.isEmpty() || langkah.isEmpty() || date == null) {
        JOptionPane.showMessageDialog(this, "Semua kolom harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(sqlDate);

    updateKontak(id, nama, bahan, langkah, tanggal);
    tampilkanResep(tableModel);
    updateListCariFromTable();
    JOptionPane.showMessageDialog(this, "Resep berhasil diubah!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_editButtonActionPerformed

    private void hapusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusButtonActionPerformed
        // TODO add your handling code here:
       int selectedRow = kontakTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String idStr = tableModel.getValueAt(selectedRow, 0).toString();
    int id = Integer.parseInt(idStr); // Konversi ke integer

    int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus resep ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        hapusKontak(id); // Panggil method hapus
        tampilkanResep(tableModel); // Refresh tabel
        updateListCariFromTable();
        JOptionPane.showMessageDialog(this, "Resep berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }
    }//GEN-LAST:event_hapusButtonActionPerformed

    private void eksporButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eksporButtonActionPerformed
        // TODO add your handling code here:
            String filePath = "Resep.csv"; // Tentukan file tujuan
       eksporKeCSV(filePath); // Ekspor data ke CSV
    }//GEN-LAST:event_eksporButtonActionPerformed

    private void imporButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imporButtonActionPerformed
        // TODO add your handling code here:
            String filePath = "Resep.csv"; // File sumber
        imporDariCSV(filePath); // Impor data dari CSV
        tampilkanResep(tableModel); // Refresh JTable
    }//GEN-LAST:event_imporButtonActionPerformed

    private void cariButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cariButtonActionPerformed
    cariButton.addActionListener(e -> {
            String keyword = caritext.getText();
            List<String[]> hasilCari = cariResep(keyword); // cariKontak adalah metode pencarian
            tableModel.setRowCount(0); // Bersihkan tabel sebelum menampilkan hasil
            for (String[] resep : hasilCari) {
                tableModel.addRow(resep);
            }
        });
    }//GEN-LAST:event_cariButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AplikasiResepMasakan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AplikasiResepMasakan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AplikasiResepMasakan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AplikasiResepMasakan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AplikasiResepMasakan().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cariButton;
    private javax.swing.JTextField caritext;
    private javax.swing.JButton editButton;
    private javax.swing.JButton eksporButton;
    private javax.swing.JButton hapusButton;
    private javax.swing.JButton imporButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable kontakTable;
    private javax.swing.JList<String> listResep;
    private javax.swing.JTextField namaTextField;
    private javax.swing.JButton tambahButton;
    private com.toedter.calendar.JDateChooser tanggalbuat;
    private javax.swing.JTextArea textbahan;
    private javax.swing.JTextArea textlangkah;
    // End of variables declaration//GEN-END:variables
}
