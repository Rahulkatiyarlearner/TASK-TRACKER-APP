package com.tasktracker;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TaskTrackerApp extends JFrame {

    private static final Color GREEN_BG = new Color(144, 238, 144);
    private static final Color RED_BG = new Color(255, 160, 160);
    private static final Color BLINK_RED = new Color(255, 99, 71);
    private static final Color BLINK_ALT = new Color(255, 220, 220);

    private final JsonStore store = new JsonStore();
    private final AppState state = store.load();
    private final ActiveTaskTableModel activeTaskTableModel = new ActiveTaskTableModel();
    private final CompletedTaskTableModel completedTaskTableModel = new CompletedTaskTableModel();
    private final JTable activeTaskTable = new JTable(activeTaskTableModel);
    private final JTable completedTaskTable = new JTable(completedTaskTableModel);
    private final JTabbedPane tasksTabs = new JTabbedPane();
    private final JTextField taskNameField = new JTextField(18);
    private JFrame completedTasksWindow;

    private final JTextField reminderNameField = new JTextField(12);
    private final JSpinner reminderHourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
    private final JSpinner reminderMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    private final JComboBox<Reminder.Recurrence> recurrenceBox = new JComboBox<>(Reminder.Recurrence.values());
    private final JComboBox<DayOfWeek> weekdayBox = new JComboBox<>(DayOfWeek.values());
    private final JSpinner monthDaySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
    private final DefaultListModel<Reminder> reminderListModel = new DefaultListModel<>();
    private final JList<Reminder> reminderList = new JList<>(reminderListModel);

    private final Map<String, TaskElapsed.VisualState> lastKnownTaskState = new HashMap<>();
    private volatile boolean blinkPhase;

    public TaskTrackerApp() {
        super("Task Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 520));

        for (Reminder r : state.getReminders()) {
            reminderListModel.addElement(r);
        }

        initTaskTables();
        initReminderList();

        JPanel top = new JPanel(new GridLayout(2, 1, 6, 6));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Task name:"));
        taskNameField.setToolTipText("Enter task name (24h clock is used for timestamps)");
        row1.add(taskNameField);
        JButton addTaskBtn = new JButton("Add Task");
        addTaskBtn.addActionListener(this::onAddTask);
        row1.add(addTaskBtn);
        JButton markCompletedBtn = new JButton("Mark selected as completed");
        markCompletedBtn.addActionListener(e -> onMarkSelectedTaskCompleted());
        row1.add(markCompletedBtn);
        JButton openCompletedWindowBtn = new JButton("Open Completed Tasks Window");
        openCompletedWindowBtn.addActionListener(e -> openCompletedTasksWindow());
        row1.add(openCompletedWindowBtn);
        JButton deleteTaskBtn = new JButton("Delete selected task");
        deleteTaskBtn.addActionListener(e -> onDeleteSelectedTask());
        row1.add(deleteTaskBtn);
        row1.add(new JLabel("  |  "));
        row1.add(new JLabel("Reminder:"));
        row1.add(reminderNameField);
        row1.add(new JLabel("Time (24h):"));
        row1.add(reminderHourSpinner);
        row1.add(new JLabel(":"));
        row1.add(reminderMinuteSpinner);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Repeat:"));
        row2.add(recurrenceBox);
        row2.add(new JLabel("Weekday (weekly):"));
        row2.add(weekdayBox);
        row2.add(new JLabel("Day of month (monthly):"));
        row2.add(monthDaySpinner);
        JLabel dataPath = new JLabel("Data: " + store.getFile().toAbsolutePath());
        dataPath.setFont(dataPath.getFont().deriveFont(Font.PLAIN, 11f));
        row2.add(new JLabel("    "));
        row2.add(dataPath);

        recurrenceBox.addActionListener(e -> updateRecurrenceControls());
        updateRecurrenceControls();

        top.add(row1);
        top.add(row2);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        tasksTabs.addTab("Active", new JScrollPane(activeTaskTable));
        tasksTabs.addTab("Completed", new JScrollPane(completedTaskTable));
        tasksTabs.addChangeListener(e -> {
            boolean onActive = tasksTabs.getSelectedIndex() == 0;
            markCompletedBtn.setEnabled(onActive);
        });
        markCompletedBtn.setEnabled(tasksTabs.getSelectedIndex() == 0);
        center.add(tasksTabs, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setBorder(BorderFactory.createTitledBorder("Reminders"));
        reminderList.setVisibleRowCount(4);
        JButton addReminderBtn = new JButton("Add Reminder");
        addReminderBtn.addActionListener(this::onAddReminder);
        JButton removeReminderBtn = new JButton("Remove Reminder");
        removeReminderBtn.addActionListener(e -> onRemoveReminder());
        JPanel reminderButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reminderButtons.add(addReminderBtn);
        reminderButtons.add(removeReminderBtn);
        south.add(new JScrollPane(reminderList), BorderLayout.CENTER);
        south.add(reminderButtons, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        javax.swing.Timer clockTimer = new javax.swing.Timer(1_000, e -> onClockTick());
        clockTimer.setRepeats(true);
        clockTimer.start();

        javax.swing.Timer blinkTimer = new javax.swing.Timer(500, e -> {
            blinkPhase = !blinkPhase;
            activeTaskTable.repaint();
        });
        blinkTimer.start();

        pack();
        setLocationRelativeTo(null);
    }

    private void updateRecurrenceControls() {
        Reminder.Recurrence r = (Reminder.Recurrence) recurrenceBox.getSelectedItem();
        boolean weekly = r == Reminder.Recurrence.WEEKLY;
        boolean monthly = r == Reminder.Recurrence.MONTHLY;
        weekdayBox.setEnabled(weekly);
        monthDaySpinner.setEnabled(monthly);
    }

    private void initReminderList() {
        reminderList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Reminder) {
                    lab.setText(formatReminderLine((Reminder) value));
                }
                return lab;
            }
        });
    }

    private static String formatReminderLine(Reminder r) {
        String time = String.format("%02d:%02d", r.getHour24(), r.getMinute());
        switch (r.getRecurrence()) {
            case DAILY:
                return r.getName() + " — daily at " + time;
            case WEEKLY:
                return r.getName() + " — weekly on " + Reminder.dayOfWeekLabel(r.getDayOfWeekValue()) + " at " + time;
            case MONTHLY:
                return r.getName() + " — monthly on day " + r.getDayOfMonth() + " at " + time;
            default:
                return r.getName();
        }
    }

    private void onClockTick() {
        activeTaskTableModel.refreshElapsedColumn();
        checkTaskStatusSounds();
        checkReminders();
    }

    private void checkTaskStatusSounds() {
        for (Task t : state.getTasks()) {
            if (t.isCompleted()) {
                continue;
            }
            TaskElapsed.VisualState now = TaskElapsed.visualState(t.getCreatedAtEpochMillis());
            TaskElapsed.VisualState prev = lastKnownTaskState.put(t.getId(), now);
            if (prev == null) {
                continue;
            }
            if (prev == TaskElapsed.VisualState.UNDER_NINE_HOURS && now != TaskElapsed.VisualState.UNDER_NINE_HOURS) {
                Sounds.taskNineHourAlert();
            }
            if (now == TaskElapsed.VisualState.OVER_TWENTY_FOUR
                    && prev != TaskElapsed.VisualState.OVER_TWENTY_FOUR) {
                Sounds.taskTwentyFourHourAlert();
            }
        }
    }

    private void checkReminders() {
        long epochMinute = Instant.now().getEpochSecond() / 60;
        ZonedDateTime znow = TaskElapsed.nowLocal();
        boolean changed = false;
        for (Reminder r : state.getReminders()) {
            if (!ReminderScheduler.matchesThisMinute(r, znow)) {
                continue;
            }
            if (r.getLastFiredEpochMinute() >= epochMinute) {
                continue;
            }
            r.setLastFiredEpochMinute(epochMinute);
            changed = true;
            Sounds.reminderAlert();
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, r.getName(), "Reminder", JOptionPane.INFORMATION_MESSAGE));
        }
        if (changed) {
            persistQuiet();
        }
    }

    private void onAddTask(ActionEvent e) {
        String name = taskNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a task name.", "Add task", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Task t = new Task(name, System.currentTimeMillis());
        state.getTasks().add(t);
        activeTaskTableModel.fireTableDataChanged();
        taskNameField.setText("");
        persistQuiet();
    }

    private void onDeleteSelectedTask() {
        Task selected = getSelectedTaskFromCurrentTab();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a task row first.", "Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean removedOk = state.getTasks().remove(selected);
        if (!removedOk) {
            return;
        }
        lastKnownTaskState.remove(selected.getId());
        activeTaskTableModel.fireTableDataChanged();
        completedTaskTableModel.fireTableDataChanged();
        persistQuiet();
    }

    private void onMarkSelectedTaskCompleted() {
        if (tasksTabs.getSelectedIndex() != 0) {
            return;
        }
        Task selected = getSelectedTaskFromCurrentTab();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a task row first.", "Complete task", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selected.isCompleted()) {
            return;
        }
        selected.setCompletedAtEpochMillis(System.currentTimeMillis());
        lastKnownTaskState.remove(selected.getId());
        activeTaskTableModel.fireTableDataChanged();
        completedTaskTableModel.fireTableDataChanged();
        persistQuiet();
    }

    private Task getSelectedTaskFromCurrentTab() {
        int tab = tasksTabs.getSelectedIndex();
        JTable table = tab == 1 ? completedTaskTable : activeTaskTable;
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (tab == 1) {
            return completedTaskTableModel.getTaskAt(modelRow);
        }
        return activeTaskTableModel.getTaskAt(modelRow);
    }

    private void onAddReminder(ActionEvent e) {
        String name = reminderNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a reminder name.", "Reminder", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Reminder r = new Reminder();
        r.setName(name);
        r.setHour24((Integer) reminderHourSpinner.getValue());
        r.setMinute((Integer) reminderMinuteSpinner.getValue());
        Reminder.Recurrence rec = (Reminder.Recurrence) recurrenceBox.getSelectedItem();
        r.setRecurrence(Objects.requireNonNullElse(rec, Reminder.Recurrence.DAILY));
        DayOfWeek dow = (DayOfWeek) weekdayBox.getSelectedItem();
        r.setDayOfWeekValue(dow != null ? dow.getValue() : DayOfWeek.MONDAY.getValue());
        r.setDayOfMonth((Integer) monthDaySpinner.getValue());
        r.setLastFiredEpochMinute(0);
        state.getReminders().add(r);
        reminderListModel.addElement(r);
        reminderNameField.setText("");
        persistQuiet();
    }

    private void onRemoveReminder() {
        Reminder r = reminderList.getSelectedValue();
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Select a reminder to remove.", "Reminders", JOptionPane.WARNING_MESSAGE);
            return;
        }
        state.getReminders().remove(r);
        reminderListModel.removeElement(r);
        persistQuiet();
    }

    private void persistQuiet() {
        try {
            store.save(state);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save data:\n" + ex.getMessage(),
                    "Save error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCompletedTasksWindow() {
        if (completedTasksWindow != null) {
            completedTasksWindow.setVisible(true);
            completedTasksWindow.toFront();
            completedTasksWindow.requestFocus();
            return;
        }

        JFrame w = new JFrame("Completed Tasks");
        w.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        w.setMinimumSize(new Dimension(820, 420));

        JTable table = new JTable(completedTaskTableModel);
        initTaskTable(table, completedTaskTableModel);
        w.setLayout(new BorderLayout());
        w.add(new JScrollPane(table), BorderLayout.CENTER);

        w.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                completedTasksWindow = null;
            }
        });

        w.pack();
        w.setLocationRelativeTo(this);
        completedTasksWindow = w;
        w.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new TaskTrackerApp().setVisible(true));
    }

    private abstract class BaseTaskTableModel extends AbstractTableModel {
        private final String[] cols = {
                "Task number",
                "Task Name",
                "Task addition date",
                "Time passed after Task creation"
        };

        void refreshElapsedColumn() {
            fireTableRowsUpdated(0, Math.max(0, getRowCount() - 1));
        }

        @Override
        public int getRowCount() {
            return getFilteredCount();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        abstract int getFilteredCount();

        abstract Task getTaskAt(int filteredRowIndex);

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Task t = getTaskAt(rowIndex);
            switch (columnIndex) {
                case 0:
                    return String.format("%02d", rowIndex + 1);
                case 1:
                    return t.getName();
                case 2:
                    return TaskElapsed.formatCreated(t.getCreatedAtEpochMillis());
                case 3:
                    return TaskElapsed.formatElapsedRunning(t.getCreatedAtEpochMillis());
                default:
                    return "";
            }
        }
    }

    private class ActiveTaskTableModel extends BaseTaskTableModel {
        @Override
        int getFilteredCount() {
            int n = 0;
            for (Task t : state.getTasks()) {
                if (!t.isCompleted()) {
                    n++;
                }
            }
            return n;
        }

        @Override
        Task getTaskAt(int filteredRowIndex) {
            int i = 0;
            for (Task t : state.getTasks()) {
                if (t.isCompleted()) {
                    continue;
                }
                if (i == filteredRowIndex) {
                    return t;
                }
                i++;
            }
            return new Task("(missing)", 0);
        }
    }

    private class CompletedTaskTableModel extends BaseTaskTableModel {
        @Override
        int getFilteredCount() {
            int n = 0;
            for (Task t : state.getTasks()) {
                if (t.isCompleted()) {
                    n++;
                }
            }
            return n;
        }

        @Override
        Task getTaskAt(int filteredRowIndex) {
            int i = 0;
            for (Task t : state.getTasks()) {
                if (!t.isCompleted()) {
                    continue;
                }
                if (i == filteredRowIndex) {
                    return t;
                }
                i++;
            }
            return new Task("(missing)", 0);
        }
    }

    private class TaskRowRenderer extends DefaultTableCellRenderer {
        private final BaseTaskTableModel model;

        TaskRowRenderer(BaseTaskTableModel model) {
            this.model = model;
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int mr = table.convertRowIndexToModel(row);
            if (mr < 0 || mr >= model.getRowCount()) {
                c.setBackground(Color.WHITE);
                return c;
            }
            Task t = model.getTaskAt(mr);
            if (t.isCompleted()) {
                c.setBackground(isSelected ? new Color(220, 220, 220) : new Color(240, 240, 240));
                c.setForeground(Color.DARK_GRAY);
                return c;
            }
            TaskElapsed.VisualState vs = TaskElapsed.visualState(t.getCreatedAtEpochMillis());
            Color bg;
            switch (vs) {
                case UNDER_NINE_HOURS:
                    bg = GREEN_BG;
                    break;
                case NINE_TO_TWENTY_FOUR:
                    bg = RED_BG;
                    break;
                case OVER_TWENTY_FOUR:
                    bg = blinkPhase ? BLINK_RED : BLINK_ALT;
                    break;
                default:
                    bg = Color.WHITE;
            }
            if (isSelected) {
                c.setBackground(bg.darker());
            } else {
                c.setBackground(bg);
            }
            c.setForeground(Color.BLACK);
            return c;
        }
    }

    private void initTaskTables() {
        initTaskTable(activeTaskTable, activeTaskTableModel);
        initTaskTable(completedTaskTable, completedTaskTableModel);
    }

    private void initTaskTable(JTable table, BaseTaskTableModel model) {
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        TaskRowRenderer renderer = new TaskRowRenderer(model);
        for (int c = 0; c < table.getColumnCount(); c++) {
            TableColumn col = table.getColumnModel().getColumn(c);
            col.setCellRenderer(renderer);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(360);
    }
}
