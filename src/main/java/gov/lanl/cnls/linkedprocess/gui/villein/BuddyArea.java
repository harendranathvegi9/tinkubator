package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.gui.JTreeImage;
import gov.lanl.cnls.linkedprocess.gui.TreeNodeProperty;
import gov.lanl.cnls.linkedprocess.gui.TreeRenderer;
import gov.lanl.cnls.linkedprocess.xmpp.villein.FarmStruct;
import gov.lanl.cnls.linkedprocess.xmpp.villein.HostStruct;
import gov.lanl.cnls.linkedprocess.xmpp.villein.Struct;
import gov.lanl.cnls.linkedprocess.xmpp.villein.VmStruct;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 11:13:22 PM
 */
public class BuddyArea extends JPanel implements ActionListener, MouseListener {

    protected VilleinGui villeinGui;
    protected JTreeImage tree;
    protected JTextField addBuddyField;
    protected JPopupMenu popupMenu;
    protected Object popupTreeObject;
    protected DefaultMutableTreeNode treeRoot;
    protected DefaultMutableTreeNode villeinNode;

    public BuddyArea(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
        HostStruct hostStruct = new HostStruct();
        hostStruct.setFullJid(LinkedProcess.generateBareJid(this.villeinGui.getXmppVillein().getFullJid()));
        this.treeRoot = new DefaultMutableTreeNode(hostStruct);
        this.tree = new JTreeImage(this.treeRoot, ImageHolder.cowBackground);
        this.tree.setCellRenderer(new TreeRenderer());
        this.tree.setModel(new DefaultTreeModel(treeRoot));
        this.tree.addMouseListener(this);
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));

        JScrollPane vmTreeScroll = new JScrollPane(this.tree);
        JButton shutdownButton = new JButton("shutdown");
        JButton addFarmButton = new JButton("add farm");
        shutdownButton.addActionListener(this);
        addFarmButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.addBuddyField = new JTextField(15);
        buttonPanel.add(this.addBuddyField);
        buttonPanel.add(addFarmButton);
        buttonPanel.add(shutdownButton);

        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(buttonPanel, BorderLayout.SOUTH);
        treePanel.setOpaque(false);
        treePanel.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));

        this.add(treePanel);

        this.villeinGui.getXmppVillein().createHostStructsFromRoster();
        this.createTree();
    }

    public void actionPerformed(ActionEvent event) {

        this.popupMenu.setVisible(false);
        if (event.getActionCommand().equals("add farm")) {
            if (this.addBuddyField.getText() != null && this.addBuddyField.getText().length() > 0)
                this.villeinGui.getXmppVillein().requestSubscription(this.addBuddyField.getText());
        } else if (event.getActionCommand().equals("unsubscribe")) {
            if (this.popupTreeObject instanceof HostStruct) {
                String jid = ((HostStruct) this.popupTreeObject).getFullJid();
                this.villeinGui.getXmppVillein().requestUnsubscription(jid, true);
                this.popupTreeObject = null;
            }
        } else if (event.getActionCommand().equals("terminate vm")) {
            if (this.popupTreeObject instanceof VmStruct) {
                this.villeinGui.getXmppVillein().terminateVirtualMachine((VmStruct) this.popupTreeObject);
            }
        } else if (event.getActionCommand().equals("JavaScript")) {
            if (this.popupTreeObject instanceof FarmStruct) {
                String farmJid = ((FarmStruct) this.popupTreeObject).getFullJid();
                this.villeinGui.getXmppVillein().spawnVirtualMachine(farmJid, "JavaScript");
            }
        } else if (event.getActionCommand().equals("shutdown")) {

            this.villeinGui.getXmppVillein().shutDown();
            this.villeinGui.loadLoginFrame();
        }


    }

    public void createTree() {
        treeRoot.removeAllChildren();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        this.villeinNode = new DefaultMutableTreeNode(this.villeinGui.getXmppVillein());
        for (HostStruct hostStruct : this.villeinGui.getXmppVillein().getHostStructs()) {
            DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(hostStruct);
            for (FarmStruct farmStruct : hostStruct.getFarmStructs()) {
                DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(farmStruct);
                for (VmStruct vmStruct : farmStruct.getVmStructs()) {
                    DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vmStruct);
                    model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                    DefaultMutableTreeNode temp;

                    if (vmStruct.getPresence() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vmStruct.getPresence().getType().toString()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                    }
                    if (vmStruct.getVmSpecies() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vmStruct.getVmSpecies()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                    }
                    if (vmStruct.getVmPassword() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmStruct.getVmPassword()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                    }
                }
                model.insertNodeInto(farmNode, hostNode, hostNode.getChildCount());
                this.tree.scrollPathToVisible(new TreePath(farmNode.getPath()));
            }

            model.insertNodeInto(hostNode, villeinNode, villeinNode.getChildCount());
            this.tree.scrollPathToVisible(new TreePath(hostNode.getPath()));
        }
        model.insertNodeInto(villeinNode, this.treeRoot, this.treeRoot.getChildCount());
        this.tree.scrollPathToVisible(new TreePath(villeinNode.getPath()));
        model.reload();
    }

    /*private void getAllChildren(DefaultMutableTreeNode node, Set<DefaultMutableTreeNode> children) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            children.add(child);
            getAllChildren(child, children);
        }
    }*/

    private DefaultMutableTreeNode getNode(DefaultMutableTreeNode root, String jid) {
        if (root.getUserObject() instanceof Struct) {
            Struct temp = (Struct) root.getUserObject();
            if (temp.getFullJid().equals(jid)) {
                return root;
            }
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode node = getNode((DefaultMutableTreeNode) root.getChildAt(i), jid);
            if(node != null)
                return node;
        }
        return null;
    }

    public void updateTree(String jid, boolean remove) {
        DefaultMutableTreeNode node = this.getNode(this.treeRoot, jid);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

        if (node != null) {
            if (remove) {
                node.removeAllChildren();
                model.removeNodeFromParent(node);
            } else {
                if (node.getUserObject() instanceof HostStruct) {
                    this.tree.scrollPathToVisible(new TreePath(node.getPath()));
                    model.reload(node);
                } else if (node.getUserObject() instanceof FarmStruct) {
                    this.tree.scrollPathToVisible(new TreePath(node.getPath()));
                    model.reload(node);
                } else {
                    node.removeAllChildren();
                    VmStruct vmStruct = (VmStruct) node.getUserObject();
                    DefaultMutableTreeNode temp;

                    if (vmStruct.getPresence() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vmStruct.getPresence().getType().toString()));
                        model.insertNodeInto(temp, node, node.getChildCount());
                    }
                    if (vmStruct.getVmSpecies() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vmStruct.getVmSpecies()));
                        model.insertNodeInto(temp, node, node.getChildCount());
                    }
                    if (vmStruct.getVmPassword() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmStruct.getVmPassword()));
                        model.insertNodeInto(temp, node, node.getChildCount());
                    }
                    model.reload(node);
                }
            }
        } else {
            if (!remove) {
                Struct parentStruct = this.villeinGui.getXmppVillein().getParentStruct(jid);
                DefaultMutableTreeNode parentNode = null;
                if (parentStruct != null) {
                    parentNode = this.getNode(this.treeRoot, parentStruct.getFullJid());
                }

                Struct struct = this.villeinGui.getXmppVillein().getStruct(jid);
                if (struct instanceof HostStruct) {
                    DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(struct);
                    model.insertNodeInto(hostNode, villeinNode, villeinNode.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(hostNode.getPath()));
                    model.reload(hostNode);
                } else if (struct instanceof FarmStruct) {
                    if (parentNode != null) {
                        DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(struct);
                        model.insertNodeInto(farmNode, parentNode, parentNode.getChildCount());
                        this.tree.scrollPathToVisible(new TreePath(farmNode.getPath()));
                        model.reload(farmNode);
                    } else {
                        parentStruct = this.villeinGui.getXmppVillein().getParentStruct(LinkedProcess.generateBareJid(jid));
                        parentNode = this.getNode(this.treeRoot, parentStruct.getFullJid());
                        if (parentNode != null) {
                            DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(struct);
                            model.insertNodeInto(farmNode, parentNode, parentNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(farmNode.getPath()));
                            model.reload(farmNode);
                        }
                    }
                } else if (struct instanceof VmStruct) {
                    if (parentNode != null) {
                        VmStruct vmStruct = (VmStruct) struct;
                        DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(struct);
                        DefaultMutableTreeNode temp;
                        
                        if (vmStruct.getPresence() != null) {
                            temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vmStruct.getPresence().getType().toString()));
                            model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(temp.getPath()));
                        }
                        if (vmStruct.getVmSpecies() != null) {
                            temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vmStruct.getVmSpecies()));
                            model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(temp.getPath()));
                        }
                        if (vmStruct.getVmPassword() != null) {
                            temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmStruct.getVmPassword()));
                            model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(temp.getPath()));
                        }

                        model.insertNodeInto(vmNode, parentNode, parentNode.getChildCount());
                        this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                        model.reload(vmNode);

                    }
                }
            }
        }
    }

    public void mouseClicked(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        int selectedRow = tree.getRowForLocation(x, y);
        if (selectedRow != -1) {
            if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1) {
                TreePath selectedPath = tree.getPathForLocation(x, y);
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                Object nodeObject = selectedNode.getUserObject();
                if (nodeObject instanceof HostStruct) {
                    this.popupTreeObject = nodeObject;
                    popupMenu.removeAll();
                    JLabel menuLabel = new JLabel("Host");
                    JMenuItem unsubscribeItem = new JMenuItem("unsubscribe");
                    menuLabel.setHorizontalTextPosition(JLabel.CENTER);
                    popupMenu.add(menuLabel);
                    popupMenu.addSeparator();
                    popupMenu.add(unsubscribeItem);
                    unsubscribeItem.addActionListener(this);
                    popupMenu.setLocation(x + villeinGui.getX(), y + villeinGui.getY());
                    popupMenu.setVisible(true);
                } else if (nodeObject instanceof VmStruct) {
                    this.popupTreeObject = nodeObject;
                    popupMenu.removeAll();
                    JLabel menuLabel = new JLabel("Virtual Machine");
                    JMenuItem terminateVmItem = new JMenuItem("terminate vm");
                    menuLabel.setHorizontalTextPosition(JLabel.CENTER);
                    popupMenu.add(menuLabel);
                    popupMenu.addSeparator();
                    popupMenu.add(terminateVmItem);
                    terminateVmItem.addActionListener(this);
                    popupMenu.setLocation(x + villeinGui.getX(), y + villeinGui.getY());
                    popupMenu.setVisible(true);
                } else if (nodeObject instanceof FarmStruct) {
                    this.popupTreeObject = nodeObject;
                    popupMenu.removeAll();
                    JLabel menuLabel = new JLabel("Farm");
                    JMenuItem spawnItem = new JMenuItem("spawn vm");
                    JMenuItem javaScriptItem = new JMenuItem("JavaScript");
                    spawnItem.add(javaScriptItem);
                    menuLabel.setHorizontalTextPosition(JLabel.CENTER);
                    popupMenu.add(menuLabel);
                    popupMenu.addSeparator();
                    popupMenu.add(spawnItem);
                    javaScriptItem.addActionListener(this);
                    popupMenu.setLocation(x + villeinGui.getX(), y + villeinGui.getY());
                    popupMenu.setVisible(true);
                    // todo: make popup submenu work correctly, though the bug is still functional
                }

            } else if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() > 1) {
                TreePath selectedPath = tree.getPathForLocation(x, y);
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                Object nodeObject = selectedNode.getUserObject();
                if (nodeObject instanceof VmStruct) {
                    VmStruct vmStruct = (VmStruct)nodeObject;
                    VmFrame vmFrame = this.villeinGui.getVmFrame(vmStruct.getFullJid());
                    if(vmFrame == null) {
                        this.villeinGui.addVmFrame(vmStruct);
                    } else {
                        vmFrame.setVisible(true);
                    }

                }
            }

        }

    }

    public void mouseReleased(MouseEvent e) {
        this.popupMenu.setVisible(false);
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent event) {

    }


}
