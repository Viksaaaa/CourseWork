import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;

import static org.junit.jupiter.api.Assertions.*;

class AikidoSportsmenTest {

    @Test
    void testClearModelMethod()
    {
        String[][] data = new String[][]{{"СК Волна", "Евсеева Виктория Денисовна", "Томики"},
                {"СК Ленкай", "Новиков Иван Анатольевич", "Айкикай"},
                {"СК Сейбукан", "Беляев Михаил Григорьевич", "Ёсинкан"},
                {"СК Волна", "Владимирова Екатерина Евгеньевна", "Томики"},
                {"СК Ленкай", "Сапожников Дмитрий Сергеевич", "Айкикай"}};;
        String[] columns = new String[]{"Клуб", "ФИО", "Стиль"};
        DefaultTableModel model = new DefaultTableModel(data, columns);
//
//        assert setup < 1 : "Nothing to clear";
        int result = AikidoSportsmen.clearModel(model);
        assertEquals(0, result);
    }

    @Test
    void testEmptyModelMethod()
    {
        String[][] data = new String[][]{{"СК Волна", "Евсеева Виктория Денисовна", "Томики"},
                {"СК Ленкай", "Новиков Иван Анатольевич", "Айкикай"},
                {"СК Сейбукан", "Беляев Михаил Григорьевич", "Ёсинкан"},
                {"СК Волна", "Владимирова Екатерина Евгеньевна", "Томики", "Брежнев Андрей Николаевич"},
                {"СК Ленкай", "Сапожников Дмитрий Сергеевич", "Айкикай"}};;
        String[] columns = new String[]{"Клуб", "ФИО", "Стиль"};
        DefaultTableModel model = new DefaultTableModel(data, columns);
        int setup = AikidoSportsmen.countRowsModel(model);
        assertNotNull(setup);
        assertEquals(5, setup);
    }
}