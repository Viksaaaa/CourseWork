import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.sql.*;


public class AikidoTrainers
{
    // Объявление графических компонентов
    private JFrame aikidoTrainers;
    private DefaultTableModel model;
    private JButton save;
    private JButton open;
    private JButton add;
    private JButton edit;
    private JButton remove;
    private JButton print;
    private JButton thread;
    private JButton xml;
    private JButton xmlLoad;
    private JButton sql;
    private Connection connection;


    private JToolBar toolBar;
    private JScrollPane scroll;
    private JTable trainers;
    private JComboBox sportclub;
    private JTextField trainer;
    private JButton filter;

    public static int clearModel(DefaultTableModel mod)
    {
        int rows = mod.getRowCount();
        for (int i = 0; i < rows; i++) mod.removeRow(0); // Очистка модели
        return mod.getRowCount();
    }

    private class MyException extends Exception
    {
        public MyException()
        {
            super ("Вы не ввели ФИО тренера для поиска");
        }
    }

    private void checkName (JTextField trName) throws MyException, NullPointerException
    {
        String sName = trName.getText();
        if (sName.contains("ФИО тренера")) throw new MyException();
        if (sName.length() == 0) throw new NullPointerException();
    }

    public class WriterThread implements Runnable {
        final Object mutex;

        WriterThread(Object mutex) { this.mutex = mutex; }

        public void run(){
            System.out.println("Входим в режим записи");
            synchronized(mutex) {
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Создание");

                FileDialog save = new FileDialog(aikidoTrainers, "Сохранение данных", FileDialog.SAVE);
                save.setFile("*.txt");
                save.setVisible(true); // Отобразить запрос пользователю

                // Определить имя выбранного каталога и файла
                String fileName = save.getDirectory() + save.getFile();
                if (fileName == null) return; // Если пользователь нажал отмена

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

                    // Создание корневого элемента trainerlist и добавление его в документ

                    Node trainerlist = doc.createElement("trainerlist");
                    doc.appendChild(trainerlist);

                    // Создание дочерних элементов book и присвоение значений атрибутам

                    for (int i = 0; i < model.getRowCount(); i++) {
                        Element trainer = doc.createElement("trainer");
                        trainerlist.appendChild(trainer);
                        trainer.setAttribute("sportclub", (String) model.getValueAt(i, 0));
                        trainer.setAttribute("trainer", (String) model.getValueAt(i, 1));
                        trainer.setAttribute("style", (String) model.getValueAt(i, 2));
                    }

                    //Создание преобразователя документа
                    Transformer trans = TransformerFactory.newInstance().newTransformer();
                    // Создание файла с именем books.xml для записи документа
                    java.io.FileWriter fw = new FileWriter("trainers.xml");
                    //Запись документа в файл
                    trans.transform(new DOMSource(doc), new StreamResult(fw));
                } catch (ParserConfigurationException excep4) {
                    excep4.printStackTrace();
                } catch (TransformerConfigurationException exc1) {
                    exc1.printStackTrace();
                } catch (TransformerException exc2) {
                    exc2.printStackTrace();
                } catch (IOException exc3) {
                    exc3.printStackTrace();
                }

                System.out.println("Завершаем чтение");
            }
        }
    }
    public void show()
    {
        // Создание окна

        aikidoTrainers = new JFrame("Список тренеров");
        aikidoTrainers.setSize(500,500);
        aikidoTrainers.setLocation(100,100);
        aikidoTrainers.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создание кнопок и прикрепление иконок

        save = new JButton(new ImageIcon("./image/save.png"));
        open = new JButton(new ImageIcon("./image/open.png"));
        add = new JButton(new ImageIcon("./image/add.png"));
        edit = new JButton(new ImageIcon("./image/edit.png"));
        remove = new JButton(new ImageIcon("./image/delete.png"));
        print = new JButton(new ImageIcon("./image/printer.png"));
        thread = new JButton(new ImageIcon("./image/flow.png"));
        xml = new JButton(new ImageIcon("./image/xml.png"));
        xmlLoad = new JButton(new ImageIcon("./image/xmlLoad.png"));
        sql = new JButton(new ImageIcon("./image/sql.png"));

        // Настройка подсказок для кнопок

        save.setToolTipText("Сохранить список тренеров");
        open.setToolTipText("Открыть новый список тренеров");
        add.setToolTipText("Добавить новый список тренеров");
        edit.setToolTipText("Редактирование списка тренеров");
        remove.setToolTipText("Удалить список тренеров");
        print.setToolTipText("Печать списка тренеров");
        thread.setToolTipText("Работа потоков");
        xml.setToolTipText("Создание xml-файла");
        xmlLoad.setToolTipText("Загрузка xml-файла");
        sql.setToolTipText("MySQL");

        // Добавление кнопок на панель инструментов

        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(save); // save file
        toolBar.add(open); // open file
        toolBar.add(add); // add row
        toolBar.add(edit); // edit table
        toolBar.add(remove); // remove table
        toolBar.add(print); // print table
        toolBar.add(thread); // threads
        toolBar.add(xml); // save xml-file
        toolBar.add(xmlLoad); // download xml-file
        toolBar.add(sql); // work with MySQL

        // Размещение панели инструментов

        aikidoTrainers.setLayout(new BorderLayout());
        aikidoTrainers.add(toolBar,BorderLayout.NORTH);

        String[] columns = {"Клуб", "ФИО тренера", "Стиль"};
        String[][] data = {{"СК Волна", "Брежнев Андрей Николаевич", "Томики"},
                            {"СК Ленкай", "Новиков Иван Анатольевич", "Айкикай"},
                            {"СК Сейбукан", "Беляев Михаил Григорьевич", "Ёсинкан"},
                            {"СК Волна", "Владимирова Екатерина Евгеньевна", "Томики"},
                            {"СК Ленкай", "Сапожников Дмитрий Сергеевич", "Айкикай"}};

        model       = new DefaultTableModel(data, columns);
        trainers    = new JTable(model);
        scroll      = new JScrollPane(trainers);

        // Размещение таблицы с данными

        aikidoTrainers.add(scroll, BorderLayout.CENTER);

        // Подготовка компонентов поиска

        sportclub   = new JComboBox(new String[]{"Клуб", "СК Волна", "СК Ленкай", "СК Сейбукан"});
        trainer     = new JTextField("ФИО тренера");
        filter      = new JButton("Поиск");

        // Добавление компонентов на панель

         JPanel filterPanel = new JPanel();
         filterPanel.add(sportclub);
         filterPanel.add(trainer);
         filterPanel.add(filter);

         // Размещение панели поиска внизу окна

        aikidoTrainers.add(filterPanel, BorderLayout.SOUTH);

        // Визуализация экранной формы

        aikidoTrainers.setVisible(true);

        // Проверка работы кнопок

        save.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                FileDialog save = new FileDialog(aikidoTrainers, "Сохранение данных", FileDialog.SAVE);
                save.setFile("*.txt");
                save.setVisible(true); // Отобразить запрос пользователю

                // Определить имя выбранного каталога и файла
                String fileName = save.getDirectory() + save.getFile();
                if (fileName == null) return; // Если пользователь нажал отмена

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
            }
        });

        open.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                FileDialog open = new FileDialog(aikidoTrainers, "Открытие данных", FileDialog.LOAD);
                open.setFile("*.txt");
                open.setVisible(true); // Отобразить запрос аротзователю

                // Определить имя выбранного каталога и файла
                String fileName = open.getDirectory() + open.getFile();
                if (fileName == null) return; // Если пользователь нажал отмена

                try
                {
                    BufferedReader reader = new BufferedReader(new FileReader(fileName));
                    int rows = model.getRowCount();
                    for (int i = 0; i < rows; i++) model.removeRow(0); // Очистка таблицы
                    String sportclub;
                    do
                    {
                        sportclub = reader.readLine();
                        if (sportclub != null)
                        {
                            String trainer = reader.readLine();
                            String style = reader.readLine();
                            model.addRow(new String[] {sportclub, trainer, style}); // Запись строки в таблицу
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
                model.addRow(new String[]{"", "", ""});
                JOptionPane.showMessageDialog(aikidoTrainers, "Пустая строка добавлена!");
            }
        });

        edit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(aikidoTrainers, "Проверка нажатия кнопки");
            }
        });

        remove.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int idx = trainers.getSelectedRow();
                // удаление
                model.removeRow(idx);
                JOptionPane.showMessageDialog(aikidoTrainers, "Строка удалена!");
            }
        });

        print.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(aikidoTrainers, "Проверка нажатия кнопки");
            }
        });

        filter.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                try
                {
                    checkName(trainer);
                }

                catch (NullPointerException ex)
                {
                    JOptionPane.showMessageDialog(aikidoTrainers, ex.toString());
                }

                catch (MyException myEx)
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

                    // Создание корневого элемента trainerslst и добавление его в документ

                    Node trainerslst = doc.createElement("trainerslst");
                    doc.appendChild(trainerslst);

                    // Создание дочерних элементов book и присвоение значений атрибутам

                    for (int i = 0; i < model.getRowCount(); i++)
                    {
                        Element trainer = doc.createElement("trainer");
                        trainerslst.appendChild(trainer);
                        trainer.setAttribute("sportclub", (String)model.getValueAt(i, 0));
                        trainer.setAttribute("trainer", (String)model.getValueAt(i, 1));
                        trainer.setAttribute("style", (String)model.getValueAt(i, 2));
                    }

                    //Создание преобразователя документа
                    Transformer trans = TransformerFactory.newInstance().newTransformer();
                    // Создание файла с именем books.xml для записи документа
                    java.io.FileWriter fw = new FileWriter("trainers.xml");
                    //Запись документа в файл
                    trans.transform(new DOMSource(doc), new StreamResult(fw));
                }

                catch (ParserConfigurationException excep4)
                {
                    excep4.printStackTrace();
                }

                catch (TransformerConfigurationException exc1)
                {
                    exc1.printStackTrace();
                }

                catch (TransformerException exc2)
                {
                    exc2.printStackTrace();
                }

                catch (IOException exc3)
                {
                    exc3.printStackTrace();
                }
            }
        });

        xmlLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int rows = model.getRowCount();
                    for (int i = 0; i < rows; i++) model.removeRow(0);
                    //DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                    // Создание парсера документа
                    DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();


                    Document doc = dBuilder.parse(new File("trainers.xml"));
                    // Чтение из файла

                    // Нормализация документа
                    doc.getDocumentElement().normalize();

                    //
                    NodeList nlTrainers = doc.getElementsByTagName("trainer");

                    //
                    for (int temp = 0; temp < nlTrainers.getLength(); temp++)
                    {
                        Node elem = nlTrainers.item(temp);
                        NamedNodeMap attrs = elem.getAttributes();
                        String sportclub = attrs.getNamedItem("sportclub").getNodeValue();
                        String trainer = attrs.getNamedItem("trainer").getNodeValue();
                        String style = attrs.getNamedItem("style").getNodeValue();
                        System.out.println(Arrays.toString(new String[]{sportclub, trainer, style}));

                        model.addRow(new String[]{sportclub, trainer, style});
                    }

                    //JOptionPane.showMessageDialog(null, "Данные выгружены из файла");
                }
                catch (ParserConfigurationException ex)
                {
                    ex.printStackTrace();
                }
                catch (SAXException ex)
                {
                    ex.printStackTrace();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        });


        sql.addActionListener(new ActionListener() {

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
                        ResultSet resultSet = connection.createStatement().executeQuery("Select * from aikidotrainers");
                        int numberOfColumns = resultSet.getMetaData().getColumnCount();
                        ArrayList<String> strings = new ArrayList<String>();
                        while (resultSet.next()) {
                            int i = 1;
                            while(i <= numberOfColumns) {
                                if ( i == 1) i++;
                                else strings.add(resultSet.getString(i++));
                            }
                        }

                        clearModel(model);

                        for ( int i = 0; i < strings.size(); i += 3)
                        {
                            String sportclub = strings.get(i);
                            String sportsman = strings.get(i + 1);
                            String style = strings.get(i + 2);
                            model.addRow(new String[]{sportclub, sportsman, style});
                        }

                        connection.close();
                    }
                    else    System.out.println("Не получилось :(");
                }
                catch (SQLException ex) { System.out.println("Ну вот опять ОХОХО " + ex.getMessage()); }
            }
        });
    }

}
