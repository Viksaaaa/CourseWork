import com.itextpdf.text.*;

import java.io.FileOutputStream;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Stream;

public class AikidoSportsmen
{
    // Объявление графических компонентов
    private JFrame aikidoSportsmen;
    private DefaultTableModel model;
    private JTable sportsmen;
    private JTextField sportsman;

    private static class MyException extends Exception
    {
        public MyException()
        {
            super ("Вы не ввели ФИО спортсмена для поиска");
        }
    }

    public static int countRowsModel(DefaultTableModel modelka)
    {
        return modelka.getRowCount();
    }

    public static int clearModel(DefaultTableModel mod)
    {
        int rows = mod.getRowCount();
        for (int i = 0; i < rows; i++) mod.removeRow(0); // Очистка модели
        return mod.getRowCount();
    }

    private void checkName (JTextField trName) throws AikidoSportsmen.MyException, NullPointerException
    {
        String sName = trName.getText();
        if (sName.contains("ФИО спортсмена")) throw new MyException();
        if (sName.length() == 0) throw new NullPointerException();
    }

    public class WriterThread implements Runnable {
        final Object mutex;

        WriterThread(Object mutex) { this.mutex = mutex; }

        public void run(){
            System.out.println("Входим в режим записи");
            synchronized(mutex) {

                try                             { Thread.sleep(7000); }
                catch (InterruptedException e)  { throw new RuntimeException(e); }

                System.out.println("Создание");

                FileDialog save = new FileDialog(aikidoSportsmen, "Сохранение данных", FileDialog.SAVE);
                save.setFile("*.txt");
                save.setVisible(true); // Отобразить запрос пользователю

                // Определить имя выбранного каталога и файла
                String fileName = save.getDirectory() + save.getFile();

                try
                {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                    for (int i = 0; i < model.getRowCount(); i++) // Для всех строк
                        for (int j = 0; j < model.getColumnCount(); j++) // Для всех столбцов
                        {
                            writer.write((String) model.getValueAt(i, j)); // Записать значение из ячейки
                            writer.write("\n"); // Записать символ перевода каретки
                        }
                    writer.close();
                }

                catch (IOException excep1) // Ошибка записи файла
                {
                    excep1.printStackTrace();
                }

                mutex.notify();
            }
        }
    }

    public class ReaderThread implements Runnable {
        final Object mutex;

        ReaderThread(Object mutex) {
            this.mutex = mutex;
        }

        public void run() {
            System.out.println("Входим в режим чтения");
            synchronized (mutex) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Начинаем чтение");
                try {
                    // Создание парсера документа
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                    // Создание пустого документа
                    Document doc = builder.newDocument();

                    // Создание корневого элемента sportsmanList и добавление его в документ
                    Node sportsmanList = doc.createElement("sportsmanList");
                    doc.appendChild(sportsmanList);

                    // Создание дочерних элементов sportsmen и присвоение значений атрибутам
                    for (int i = 0; i < model.getRowCount(); i++) {
                        Element sportsman = doc.createElement("sportsman");
                        sportsmanList.appendChild(sportsman);
                        sportsman.setAttribute("sportclub", (String) model.getValueAt(i, 0));
                        sportsman.setAttribute("sportsman", (String) model.getValueAt(i, 1));
                        sportsman.setAttribute("style", (String) model.getValueAt(i, 2));
                        sportsman.setAttribute("trainer", (String) model.getValueAt(i, 3));
                    }

                    //Создание преобразователя документа
                    Transformer trans = TransformerFactory.newInstance().newTransformer();

                    // Создание файла с именем sportsmen.xml для записи документа
                    java.io.FileWriter fw = new FileWriter("sportsmen.xml");

                    //Запись документа в файл
                    trans.transform(new DOMSource(doc), new StreamResult(fw));
                } catch (ParserConfigurationException | TransformerException | IOException excep4) {
                    excep4.printStackTrace();
                }

                System.out.println("Завершаем чтение");
            }
        }
    }
    public void show()
    {
        // Создание окна

        aikidoSportsmen = new JFrame("Список спортсменов");
        aikidoSportsmen.setSize(700,900);
        aikidoSportsmen.setLocation(700,100);
        aikidoSportsmen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создание кнопок и прикрепление иконок

        JButton save = new JButton(new ImageIcon("../../../image/save.png"));
        JButton open = new JButton(new ImageIcon("image/open.png"));
        JButton add = new JButton(new ImageIcon("image/add.png"));
        JButton edit = new JButton(new ImageIcon("./image/edit.png"));
        JButton remove = new JButton(new ImageIcon("./image/delete.png"));
        JButton print = new JButton(new ImageIcon("./image/print.png"));
        JButton thread = new JButton(new ImageIcon("./image/thread.png"));
        JButton xml = new JButton(new ImageIcon("./image/xml.png"));
        JButton xmlLoad = new JButton(new ImageIcon("./image/xmlLoad.png"));
        JButton sqlLoad = new JButton(new ImageIcon("./image/sqlLoad.png"));
        JButton sql = new JButton(new ImageIcon("./image/sql.png"));


        // Настройка подсказок для кнопок

        save.setToolTipText("Сохранить список спортсменов");
        open.setToolTipText("Открыть новый список спортсменов");
        add.setToolTipText("Добавить пустую строку");
        edit.setToolTipText("Редактирование");
        remove.setToolTipText("Удалить выбранные строки");
        print.setToolTipText("Печать списка спортсменов");
        thread.setToolTipText("Работа потоков");
        xml.setToolTipText("Создание xml-файла");
        xmlLoad.setToolTipText("Загрузка xml-файла");
        sqlLoad.setToolTipText("Загрузка из MySQL");
        sql.setToolTipText("Выгрузка в MySQL");


        // Добавление кнопок на панель инструментов

        JToolBar toolBar = new JToolBar("Панель инструментов");
        toolBar.add(save); // save file
        toolBar.add(open); // open file
        toolBar.add(add); // add row
        toolBar.add(edit); // edit table
        toolBar.add(remove); // remove table
        toolBar.add(print); // print table
        toolBar.add(thread); // threads
        toolBar.add(xml); // save xml-file
        toolBar.add(xmlLoad); // download xml-file
        toolBar.add(sqlLoad); // work with MySQL
        toolBar.add(sql); // work with MySQL


        // Размещение панели инструментов

        aikidoSportsmen.setLayout(new BorderLayout());
        aikidoSportsmen.add(toolBar,BorderLayout.NORTH);

        String[] columns = {"ID","Клуб", "ФИО", "Стиль", "Тренер"};
        String[][] data = {{"1", "СК Волна", "Евсеева Виктория Денисовна", "Томики", "Брежнев Андрей Николаевич"},
                {"2", "СК Ленкай", "Новиков Иван Анатольевич", "Айкикай", "Новиков Иван Анатольевич"},
                {"3", "СК Сейбукан", "Беляев Михаил Григорьевич", "Ёсинкан", "Сапожников Дмитрий Сергеевич"},
                {"4", "СК Волна", "Владимирова Екатерина Евгеньевна", "Томики", "Брежнев Андрей Николаевич"},
                {"5", "СК Ленкай", "Сапожников Дмитрий Сергеевич", "Айкикай", "Новиков Иван Анатольевич"}};

        model = new DefaultTableModel(data, columns);
        sportsmen = new JTable(model);
        JScrollPane scroll = new JScrollPane(sportsmen);

        // Размещение таблицы с данными

        aikidoSportsmen.add(scroll, BorderLayout.CENTER);

        // Подготовка компонентов поиска

        JComboBox sportClub = new JComboBox(new String[]{"ID", "Клуб", "ФИО", "Стиль", "Тренер"});
        sportsman = new JTextField("Данные");
        JButton filter = new JButton("Поиск");

        // Добавление компонентов на панель

        JPanel filterPanel = new JPanel();
        filterPanel.add(sportClub);
        filterPanel.add(sportsman);
        filterPanel.add(filter);

        // Размещение панели поиска внизу окна

        aikidoSportsmen.add(filterPanel, BorderLayout.SOUTH);

        // Визуализация экранной формы

        aikidoSportsmen.setVisible(true);

        // Проверка работы кнопок

        save.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                FileDialog save = new FileDialog(aikidoSportsmen, "Сохранение данных", FileDialog.SAVE);
                save.setFile("*.txt");
                save.setVisible(true); // Отобразить запрос пользователю

                // Определить имя выбранного каталога и файла
                String fileName = save.getDirectory() + save.getFile();

                try
                {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                    for (int i = 0; i < model.getRowCount(); i++) // Для всех строк
                    {
                        for (int j = 1; j < model.getColumnCount(); j++) // Для всех столбцов
                        {
                            writer.write((String) model.getValueAt(i, j) + "\n"); // Записать значение из ячейки
                        }
                        //writer.write("\n"); // Записать символ перевода каретк
                    }
                    writer.close();
                }

                catch (IOException excep1) // Ошибка записи файла
                {
                    excep1.printStackTrace();
                }
            }
        });

        open.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                FileDialog open = new FileDialog(aikidoSportsmen, "Открытие данных", FileDialog.LOAD);
                open.setFile("*.txt");
                open.setVisible(true); // Отобразить запрос аротзователю

                // Определить имя выбранного каталога и файла
                String fileName = open.getDirectory() + open.getFile();

                try
                {
                    BufferedReader reader = new BufferedReader(new FileReader(fileName));
                    countRowsModel(model);
                    clearModel(model);
                    String sportclub;
                    int k = 1;
                    do
                    {
                        sportclub = reader.readLine();
                        if (sportclub != null)
                        {
                            String sportsman = reader.readLine();
                            String style = reader.readLine();
                            String trainer = reader.readLine();
                            model.addRow(new String[] {Integer.toString(k++), sportclub, sportsman, style, trainer}); // Запись строки в таблицу
                        }
                    }
                    while (sportclub != null);
                    reader.close();
                }

                catch (FileNotFoundException excep2)
                {
                    excep2.printStackTrace(); // Файл не найден
                }

                catch (IOException excep3)
                {
                    excep3.printStackTrace();
                }
            }
        });

        add.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.addRow(new String[]{});
                JOptionPane.showMessageDialog(aikidoSportsmen, "Пустая строка добавлена!");
            }
        });

        edit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(aikidoSportsmen, "Проверка нажатия кнопки");
            }
        });

        remove.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int idx = sportsmen.getSelectedRow();
                // удаление
                model.removeRow(idx);
                JOptionPane.showMessageDialog(aikidoSportsmen, "Строка удалена!");
            }
        });

        print.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();

                try {
                    PdfWriter.getInstance(document,
                            new FileOutputStream("primer2.pdf"));
                } catch (DocumentException | FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }

                document.open();

                PdfPTable table = new PdfPTable(3);

                BaseFont courier = null;
                try {
                    courier = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
                } catch (DocumentException | IOException ex) {
                    throw new RuntimeException(ex);
                }

                Font font = new Font(courier, 12, Font.NORMAL);

                Stream.of("Ика", "FIO", columns[2])
                        .forEach(columnTitle -> {
                            PdfPCell header = new PdfPCell();
                            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                            header.setBorderWidth(2);
                            header.setPhrase(new Phrase("Ика", font));
                            table.addCell(header);
                        });

                try {
                    document.add(table);
                } catch (DocumentException ex) {
                    throw new RuntimeException(ex);
                }

                document.close();
            }
        });

        filter.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                try
                {
                    checkName(sportsman);
                }

                catch (NullPointerException ex)
                {
                    JOptionPane.showMessageDialog(aikidoSportsmen, ex.toString());
                }

                catch (AikidoSportsmen.MyException myEx)
                {
                    JOptionPane.showMessageDialog(null, myEx.getMessage());
                }

            }
        });

        thread.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object mutex = new Object();

                Thread writer = new Thread(new WriterThread(mutex));
                Thread reader = new Thread(new ReaderThread(mutex));

                System.out.println("Начинаем работать");
                reader.start(); // whatever sequence, we're parallel now
                writer.start(); // so we're starting the reader first, BUT it must wait for writer
                // do we need 3rd one?
                System.out.println("Выходим");
            }
        });

        xml.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try
                {
                    // Создание парсера документа
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                    // Создание пустого документа
                    Document doc = builder.newDocument();

                    // Создание корневого элемента sportsmenlist и добавление его в документ

                    Node sportsmenlist = doc.createElement("sportsmenlist");
                    doc.appendChild(sportsmenlist);

                    // Создание дочерних элементов book и присвоение значений атрибутам

                    for (int i = 0; i < model.getRowCount(); i++)
                    {
                        Element sportsman = doc.createElement("sportsman");
                        sportsmenlist.appendChild(sportsman);
                        sportsman.setAttribute("ID", (String)model.getValueAt(i, 0));
                        sportsman.setAttribute("sportClub", (String)model.getValueAt(i, 1));
                        sportsman.setAttribute("sportsman", (String)model.getValueAt(i, 2));
                        sportsman.setAttribute("style", (String)model.getValueAt(i, 3));
                        sportsman.setAttribute("trainer", (String)model.getValueAt(i, 4));
                    }

                    //Создание преобразователя документа
                    Transformer trans = TransformerFactory.newInstance().newTransformer();
                    // Создание файла с именем books.xml для записи документа
                    java.io.FileWriter fw = new FileWriter("sportsmen.xml");
                    //Запись документа в файл
                    trans.transform(new DOMSource(doc), new StreamResult(fw));
                } catch (ParserConfigurationException | TransformerException | IOException excep4)
                {
                    excep4.printStackTrace();
                }
            }
        });

        xmlLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearModel(model);

                    // Создание парсера документа
                    DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                    Document doc = dBuilder.parse(new File("sportsmen.xml"));
                    // Чтение из файла

                    // Нормализация документа
                    doc.getDocumentElement().normalize();

                    //
                    NodeList nlSportsmens = doc.getElementsByTagName("sportsman");
                    // <sportsman sportClub="1" sportsman="СК Волна" style="Евсеева Виктория Денисовна" trainer="Томики"/>
                    for (int temp = 0; temp < nlSportsmens.getLength(); temp++)
                    {
                        Node elem = nlSportsmens.item(temp);
                        NamedNodeMap attrs = elem.getAttributes();
                        String id = String.valueOf(temp + 1);
                        String sportclub = attrs.getNamedItem("sportClub").getNodeValue();
                        String sportsman = attrs.getNamedItem("sportsman").getNodeValue();
                        String style = attrs.getNamedItem("style").getNodeValue();
                        String trainer = attrs.getNamedItem("trainer").getNodeValue();
                        System.out.println(Arrays.toString(new String[]{id, sportclub, sportsman, style, trainer}));

                        model.addRow(new String[]{id, sportclub, sportsman, style, trainer});
                    }
                }
                catch (ParserConfigurationException | SAXException | IOException ex)
                {
                    ex.printStackTrace();
                    System.out.println("ошибкииии");
                }
            }
        });

        sqlLoad.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try                     { Class.forName("com.mysql.cj.jdbc.Driver"); }
                catch (Exception ex)    { System.out.println("Connection Error"); }
                try
                {
                    Connection connection = DriverManager.getConnection("jdbc:MySql://localhost:3306/mydb",
                            "root", "31032000");  // no port at all or :3306 OR :3307
                    if (connection != null)
                    {
                        ResultSet resultSet = connection.createStatement().executeQuery("Select * from aikidosportsmen");
                        int numberOfColumns = resultSet.getMetaData().getColumnCount();
                        ArrayList<String> strings = new ArrayList<String>();
                        while (resultSet.next()) {
                            int i = 0;
                            while(i <= numberOfColumns) {
                                if ( i == 0 || i == 6) i++;
                                else strings.add(resultSet.getString(i++));
                            }
                        }

                        clearModel(model);

                        for ( int i = 0; i < strings.size(); i += 5)
                        {
                            String id = strings.get(i);
                            String sportClub = strings.get(i + 1);
                            String sportsman = strings.get(i + 2);
                            String style = strings.get(i + 3);
                            String trainer = strings.get(i + 4);
                            model.addRow(new String[]{id, sportClub, sportsman, style, trainer});
                        }

                        connection.close();
                    }
                    else    System.out.println("Не получилось :(");
                }
                catch (SQLException ex) { System.out.println("Ну вот опять ОХОХО " + ex.getMessage()); }
            }
        });

        sql.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

//                try {
//                    Class.forName("com.mysql.cj.jdbc.Driver");
//                } catch (Exception ex) {
//                    System.out.println("Connection Error");
//                }
//                try {
//                    Connection connection = DriverManager.getConnection("jdbc:MySql://localhost:3306/mydb",
//                            "root", "31032000");  // no port at all or :3306 OR :3307
//                    if (connection != null) {
//                        ResultSet resultSet = connection.createStatement().executeQuery("Select * from aikidosportsmen");
//                        int numberOfColumns = resultSet.getMetaData().getColumnCount();
//                        ArrayList<String> strings = new ArrayList<String>();
//                        while (resultSet.next()) {
//                            int i = 1;
//                            while (i <= numberOfColumns) {
//                                if (i == 1 || i == 5) i++;
//                                else strings.add(resultSet.getString(i++));
//                            }
//                        }
//
//                        clearModel(model);
//
//                        for (int i = 0; i < strings.size(); i += 3) {
//                            String sportClub = strings.get(i);
//                            String sportsman = strings.get(i + 1);
//                            String style = strings.get(i + 2);
//                            model.addRow(new String[]{sportClub, sportsman, style});
//                        }
//
//                        connection.close();
//                    } else System.out.println("Не получилось :(");
//                } catch (SQLException ex) {
//                    System.out.println("Ну вот опять ОХОХО " + ex.getMessage());
//                }
            }
        });
    }
}
