package com.billkuker.rocketry.motorsim.gui.visual;

import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.gui.visual.workbench.MotorsEditor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;


public abstract class MultiObjectEditor<OBJECT, EDITOR extends Component> extends JTabbedPane {

    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected final Frame frame;
    private final String noun;
    private final List<ObjectCreator> creators = new Vector<>();
    private final Map<OBJECT, EDITOR> objectToEditor = new HashMap<>();
    private final Map<EDITOR, OBJECT> editorToObject = new HashMap<>();
    private final Map<File, EDITOR> fileToEditor = new HashMap<>();
    private final Map<EDITOR, File> editorToFile = new HashMap<>();
    private final Set<OBJECT> dirty = new HashSet<>();


    public MultiObjectEditor(final Frame frame, final String noun) {
        this.frame = frame;
        this.noun = " " + noun.trim();
    }

    protected boolean has(OBJECT o) {
        return objectToEditor.containsKey(o);
    }

    protected void objectAdded(OBJECT o, EDITOR e) {
    }

    protected void objectRemoved(OBJECT o, EDITOR e) {
    }

    protected final void addCreator(ObjectCreator c) {
        creators.add(c);
    }

    public boolean hasDirty() {
        return dirty.size() > 0;
    }

    public final void dirty(final OBJECT o) {
        if (!dirty.contains(o))
            setTitleAt(indexOfComponent(objectToEditor.get(o)), "*" + getTitleAt(indexOfComponent(objectToEditor.get(o))));
        dirty.add(o);
    }

    private void undirty(final OBJECT o) {
        if (dirty.contains(o))
            setTitleAt(indexOfComponent(objectToEditor.get(o)), getTitleAt(indexOfComponent(objectToEditor.get(o))).replaceAll("^\\*", ""));
        dirty.remove(o);
    }

    public JMenu getMenu() {
        JMenu ret = new JMenu("File");
        for (JComponent i : getMenuItems())
            ret.add(i);
        return ret;
    }

    private void menuNew(ObjectCreator c) {
        add(c.newObject());
    }

    @SuppressWarnings("unchecked")
    public EDITOR getSelectedEditor() {
        EDITOR e = (EDITOR) super.getSelectedComponent();
        if (editorToObject.containsKey(e))
            return e;
        return null;
    }

    private void close() {
        EDITOR e = getSelectedEditor();
        if (e == null)
            return;
        OBJECT o = editorToObject.get(e);
        File f = editorToFile.get(e);

        if (dirty.contains(o)) {
            int response = JOptionPane.showConfirmDialog(this, "Object is unsaved. Save Before Closing?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                saveDialog();
            } else if (response == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        undirty(o);
        objectToEditor.remove(o);
        editorToObject.remove(e);
        fileToEditor.remove(f);
        editorToFile.remove(e);
        objectRemoved(o, e);
        remove(e);
    }

    private void saveDialog() {
        EDITOR e = getSelectedEditor();
        if (e == null)
            return;
        if (!editorToFile.containsKey(e)) {
            LOGGER.info("Editor has no file, saving as...");
            saveAsDialog();
            return;
        }
        File file = editorToFile.get(e);
        LOGGER.info("Saving to " + file.getAbsolutePath());
        try {
            saveToFile(editorToObject.get(e), file);
            undirty(editorToObject.get(e));
        } catch (IOException e1) {
            errorDialog(e1);
        }
    }

    private void saveAsDialog() {
        EDITOR e = getSelectedEditor();
        if (e == null)
            return;
        final FileDialog fd = new FileDialog(frame, "Save" + noun + " As", FileDialog.SAVE);
        OBJECT o = editorToObject.get(e);

        fd.setFile(((Motor) o).getName() + MotorsEditor.FILE_EXTENSION);

        fd.setDirectory(MotorsEditor.getLastPath());
        fd.setFilenameFilter((File dir, String name) -> name.endsWith(MotorsEditor.FILE_EXTENSION));
        fd.setVisible(true);
        if (fd.getFile() != null) {
            File file = new File(fd.getDirectory() + fd.getFile());
            try {
                o = editorToObject.get(e);
                saveToFile(o, file);
                undirty(o);
                objectToEditor.put(o, e);
                editorToObject.put(e, o);
                fileToEditor.put(file, e);
                editorToFile.put(e, file);
                setTitleAt(getSelectedIndex(), file.getName().replace(MotorsEditor.FILE_EXTENSION, ""));
            } catch (Exception e1) {
                errorDialog(e1);
            }
        }
    }

    private void openDialog() {
        final FileDialog fd = new FileDialog(frame, "Open" + noun + "...", FileDialog.LOAD);
        fd.setDirectory(MotorsEditor.getLastPath());
        fd.setFilenameFilter((File dir, String name) -> name.endsWith(MotorsEditor.FILE_EXTENSION));
        fd.setVisible(true);
        if (fd.getFile() != null) {
            File file = new File(fd.getDirectory() + fd.getFile());
            LOGGER.info("Opening File " + file.getAbsolutePath());
            if (fileToEditor.containsKey(file)) {
                LOGGER.info("File " + file.getAbsolutePath() + "Already open, focusing");
                setSelectedComponent(fileToEditor.get(file));
                return;
            }
            try {
                OBJECT o = loadFromFile(file);
                assignValues(o, file);
            } catch (Exception e) {
                errorDialog(e);
            }
        }
    }

    public final List<JComponent> getMenuItems() {
        List<JComponent> ret = new Vector<>();
        if (creators.size() == 1) {
            final ObjectCreator c = creators.get(0);
            ret.add(new JMenuItem("New " + c.getName()) {
                private static final long serialVersionUID = 1L;

                {
                    addActionListener(ae -> {
                        LOGGER.info("New");
                        menuNew(c);
                    });
                }
            });
        } else {
            ret.add(new JMenu("New") {
                private static final long serialVersionUID = 1L;

                {
                    for (final ObjectCreator c : creators) {
                        add(new JMenuItem("New " + c.getName()) {
                            private static final long serialVersionUID = 1L;

                            {
                                addActionListener(ae -> {
                                    LOGGER.info("New");
                                    menuNew(c);
                                });
                            }
                        });
                    }
                }
            });
        }
        ret.add(new JMenuItem("Open" + noun + "...") {
            private static final long serialVersionUID = 1L;

            {
                addActionListener(ae -> {
                    LOGGER.info("Open...");
                    openDialog();
                });
            }
        });
        ret.add(new JMenuItem("Close" + noun) {
            private static final long serialVersionUID = 1L;

            {
                addActionListener(ae -> {
                    LOGGER.info("Close");
                    close();
                });
            }
        });
        ret.add(new JSeparator());
        ret.add(new JMenuItem("Save" + noun) {
            private static final long serialVersionUID = 1L;

            {
                addActionListener(ae -> {
                    LOGGER.info("Save");
                    saveDialog();
                });
            }
        });
        ret.add(new JMenuItem("Save" + noun + " As...") {
            private static final long serialVersionUID = 1L;

            {
                addActionListener(ae -> {
                    LOGGER.info("Save As...");
                    saveAsDialog();
                });
            }
        });
        return ret;
    }

    protected final void add(final OBJECT o) {
        EDITOR e = createEditor(o);
        objectToEditor.put(o, e);
        editorToObject.put(e, o);
        addTab("new", e);
        dirty(o);
        objectAdded(o, e);
        setSelectedComponent(e);
    }

    protected final void add(final OBJECT o, final File f) {
        assignValues(o, f);
    }

    private void assignValues(OBJECT o, File f) {
        EDITOR e = createEditor(o);
        objectToEditor.put(o, e);
        editorToObject.put(e, o);
        fileToEditor.put(f, e);
        editorToFile.put(e, f);
        addTab(f.getName().replace(MotorsEditor.FILE_EXTENSION, ""), e);
        objectAdded(o, e);
        setSelectedComponent(e);
    }

    public final void load(final File f) throws IOException {
        OBJECT o = loadFromFile(f);
        assignValues(o, f);
    }

    public abstract EDITOR createEditor(final OBJECT o);

    protected abstract OBJECT loadFromFile(final File f) throws IOException;

    protected abstract void saveToFile(final OBJECT o, final File f) throws IOException;

    private void errorDialog(final Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(MultiObjectEditor.this, t.getClass().getSimpleName() + ": " + t.getMessage());
    }

    protected abstract class ObjectCreator {
        public abstract OBJECT newObject();

        public abstract String getName();
    }
}
