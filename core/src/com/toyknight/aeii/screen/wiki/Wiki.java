package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Ability;
import com.toyknight.aeii.entity.Status;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.screen.dialog.BasicDialog;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.TileFactory;
import com.toyknight.aeii.utils.UnitFactory;

import java.util.LinkedList;

/**
 * @author toyknight 6/12/2016.
 */
public class Wiki extends BasicDialog {

    private final LinkedList<Tree.Node> undo_queue;
    private final LinkedList<Tree.Node> redo_queue;

    private final ObjectMap<Integer, Tree.Node> unit_nodes;
    private final ObjectMap<Integer, Tree.Node> ability_nodes;
    private final ObjectMap<Integer, Tree.Node> status_nodes;

    private final Tree content_list;
    private final ScrollPane sp_content_list;
    private final Table content_page;
    private final ScrollPane sp_content_page;

    private final Label label_title;

    private final TextButton btn_previous;
    private final TextButton btn_next;

    private final GameplayPage gameplay_page;
    private final TilePage tile_page;
    private final UnitPage unit_page;
    private final AbilityPage ability_page;
    private final StatusPage status_page;
    private final MultiplayerPage multiplayer_page;
    private final AboutPage about_page;

    private Tree.Node current_node;

    private boolean clicking;

    public Wiki(StageScreen owner) {
        super(owner);
        int width = ts * 14 + ts / 4;
        int height = Gdx.graphics.getHeight() - ts;
        setBounds((Gdx.graphics.getWidth() - width) / 2, (Gdx.graphics.getHeight() - height) / 2, width, height);

        undo_queue = new LinkedList<Tree.Node>();
        redo_queue = new LinkedList<Tree.Node>();

        content_list = new Tree(getContext().getSkin());
        content_list.setPadding(ts / 4);
        content_list.setYSpacing(ts / 4);
        content_list.getSelection().setMultiple(false);
        content_list.getSelection().setRequired(true);
        content_list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onNodeSelected(content_list.getSelection().first());
            }
        });
        content_list.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Tree.Node node = content_list.getNodeAt(y);
                if (node == null) return;
                if (node != content_list.getNodeAt(getTouchDownY())) return;
                if (node.getChildren().size > 0) {
                    node.setExpanded(!node.isExpanded());
                }
            }
        });
        content_list.getStyle().background = ResourceManager.createDrawable(ResourceManager.getListBackground());
        content_list.getStyle().selection = ResourceManager.createDrawable(ResourceManager.getListSelectedBackground());

        RootNode node_overview = RootNode.create(RootNode.OVERVIEW, getContext().getSkin());
        RootNode node_gameplay = RootNode.create(RootNode.GAMEPLAY, getContext().getSkin());
        Tree.Node node_gameplay_objectives =
                EntryNode.create(EntryNode.TYPE_GAMEPLAY_OBJECTIVES, ts, getContext().getSkin());
        Tree.Node node_gameplay_recruiting =
                EntryNode.create(EntryNode.TYPE_GAMEPLAY_RECRUITING, ts, getContext().getSkin());
        Tree.Node node_gameplay_attacking =
                EntryNode.create(EntryNode.TYPE_GAMEPLAY_ATTACKING, ts, getContext().getSkin());
        Tree.Node node_gameplay_healing =
                EntryNode.create(EntryNode.TYPE_GAMEPLAY_HEALING, ts, getContext().getSkin());
        Tree.Node node_gameplay_income =
                EntryNode.create(EntryNode.TYPE_GAMEPLAY_INCOME, ts, getContext().getSkin());
        Tree.Node node_gameplay_status =
                EntryNode.create(EntryNode.TYPE_GAMEPLAY_STATUS, ts, getContext().getSkin());
        node_gameplay.add(node_gameplay_objectives);
        node_gameplay.add(node_gameplay_recruiting);
        node_gameplay.add(node_gameplay_attacking);
        node_gameplay.add(node_gameplay_healing);
        node_gameplay.add(node_gameplay_income);
        node_gameplay.add(node_gameplay_status);
        RootNode node_terrains = RootNode.create(RootNode.TERRAINS, getContext().getSkin());
        for (int index = 0; index < TileFactory.getTileCount(); index++) {
            Tree.Node node = EntryNode.create(EntryNode.TYPE_TILE, index, ts, getContext().getSkin());
            node_terrains.add(node);
        }
        RootNode node_units = RootNode.create(RootNode.UNITS, getContext().getSkin());
        unit_nodes = new ObjectMap<Integer, Tree.Node>();
        for (int index = 0; index < UnitFactory.getUnitCount(); index++) {
            if (index != UnitFactory.getCrystalIndex()) {
                Tree.Node node = EntryNode.create(EntryNode.TYPE_UNIT, index, ts, getContext().getSkin());
                unit_nodes.put(index, node);
                node_units.add(node);
            }
        }
        RootNode node_abilities = RootNode.create(RootNode.ABILITIES, getContext().getSkin());
        ability_nodes = new ObjectMap<Integer, Tree.Node>();
        for (int ability : Ability.getAllAbilities()) {
            Tree.Node node = EntryNode.create(EntryNode.TYPE_ABILITY, ability, ts, getContext().getSkin());
            ability_nodes.put(ability, node);
            node_abilities.add(node);
        }
        RootNode node_status = RootNode.create(RootNode.STATUS, getContext().getSkin());
        RootNode node_multiplayer = RootNode.create(RootNode.MULTIPLAYER, getContext().getSkin());
        Tree.Node node_multiplayer_create_game =
                EntryNode.create(EntryNode.TYPE_MULTIPLAYER_CREATE_GAME, ts, getContext().getSkin());
        Tree.Node node_multiplayer_join_game =
                EntryNode.create(EntryNode.TYPE_MULTIPLAYER_JOIN_GAME, ts, getContext().getSkin());
        Tree.Node node_multiplayer_commands =
                EntryNode.create(EntryNode.TYPE_MULTIPLAYER_COMMANDS, ts, getContext().getSkin());
        node_multiplayer.add(node_multiplayer_create_game);
        node_multiplayer.add(node_multiplayer_join_game);
        node_multiplayer.add(node_multiplayer_commands);
        status_nodes = new ObjectMap<Integer, Tree.Node>();
        for (int status : Status.getAllStatus()) {
            Tree.Node node = EntryNode.create(EntryNode.TYPE_STATUS, status, ts, getContext().getSkin());
            status_nodes.put(status, node);
            node_status.add(node);
        }
        RootNode node_about = RootNode.create(RootNode.ABOUT, getContext().getSkin());

        content_list.add(node_overview);
        content_list.add(node_gameplay);
        content_list.add(node_terrains);
        content_list.add(node_units);
        content_list.add(node_abilities);
        content_list.add(node_status);
        content_list.add(node_multiplayer);
        content_list.add(node_about);

        sp_content_list = new ScrollPane(content_list, getContext().getSkin());
        sp_content_list.getStyle().background = ResourceManager.createDrawable(ResourceManager.getListBackground());
        sp_content_list.setScrollingDisabled(true, false);
        add(sp_content_list).size(ts * 6, height - ts / 2).padLeft(ts / 4);

        Table content_pane = new Table();

        label_title = new Label("", getContext().getSkin());
        label_title.setAlignment(Align.center);
        content_pane.add(label_title).size(ts * 8, ts).row();

        content_page = new Table();
        sp_content_page = new ScrollPane(content_page);
        sp_content_page.setScrollBarPositions(false, true);
        content_pane.add(sp_content_page).size(ts * 8, height - ts * 2).row();

        Table button_pane = new Table();

        btn_previous = new TextButton("<", getContext().getSkin());
        btn_previous.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                previous();
            }
        });
        button_pane.add(btn_previous).size(ts * 2, ts / 2);
        btn_next = new TextButton(">", getContext().getSkin());
        btn_next.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                next();
            }
        });
        button_pane.add(btn_next).size(ts * 2, ts / 2).padLeft(ts / 2);
        TextButton btn_close = new TextButton(Language.getText("LB_CLOSE"), getContext().getSkin());
        btn_close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("wiki");
            }
        });
        button_pane.add(btn_close).size(ts * 2, ts / 2).padLeft(ts / 2);

        content_pane.add(button_pane).size(ts * 8, ts);

        add(content_pane).size(ts * 8, height);

        gameplay_page = new GameplayPage(this);
        tile_page = new TilePage(this);
        unit_page = new UnitPage(this);
        ability_page = new AbilityPage(this);
        status_page = new StatusPage(this);
        multiplayer_page = new MultiplayerPage(this);
        about_page = new AboutPage(this);
    }

    @Override
    public void display() {
        current_node = null;
        undo_queue.clear();
        redo_queue.clear();
        clicking = true;
        content_list.collapseAll();
        Tree.Node first_node = content_list.getNodes().first();
        content_list.getSelection().choose(first_node);
    }

    public void gotoUnitPage(int index) {
        if (unit_nodes.containsKey(index)) {
            content_list.getSelection().choose(unit_nodes.get(index));
        }
    }

    public void gotoAbilityPage(int ability) {
        if (ability_nodes.containsKey(ability)) {
            content_list.getSelection().choose(ability_nodes.get(ability));
        }
    }

    public void gotoStatusPage(int status) {
        if (status_nodes.containsKey(status)) {
            content_list.getSelection().choose(status_nodes.get(status));
        }
    }

    private void previous() {
        clicking = false;
        current_node.collapseAll();
        redo_queue.addFirst(current_node);
        Tree.Node previous_node = undo_queue.pollFirst();
        content_list.getSelection().choose(previous_node);
        clicking = true;
    }

    private void next() {
        clicking = false;
        undo_queue.addFirst(current_node);
        Tree.Node next_node = redo_queue.pollFirst();
        content_list.getSelection().choose(next_node);
        clicking = true;
    }

    private void updateButtons() {
        btn_previous.setVisible(undo_queue.size() > 0);
        btn_next.setVisible(redo_queue.size() > 0);
    }

    private void onNodeSelected(Tree.Node node) {
        node.expandTo();
        if (clicking && current_node != null) {
            redo_queue.clear();
            undo_queue.addFirst(current_node);
        }
        current_node = node;
        generatePage(node);
        updateButtons();
    }

    private void generatePage(Tree.Node node) {
        content_page.clearChildren();
        if (node instanceof RootNode) {
            generateRootPage(((RootNode) node).getType());
        }
        if (node instanceof EntryNode) {
            int type = ((EntryNode) node).getType();
            int value = ((EntryNode) node).getValue();
            switch (type) {
                case EntryNode.TYPE_ABILITY:
                    generateAbilityPage(value);
                    break;
                case EntryNode.TYPE_STATUS:
                    generateStatusPage(value);
                    break;
                case EntryNode.TYPE_TILE:
                    generateTilePage(value);
                    break;
                case EntryNode.TYPE_UNIT:
                    generateUnitPage(value);
                    break;
                case EntryNode.TYPE_GAMEPLAY_ATTACKING:
                case EntryNode.TYPE_GAMEPLAY_HEALING:
                case EntryNode.TYPE_GAMEPLAY_INCOME:
                case EntryNode.TYPE_GAMEPLAY_OBJECTIVES:
                case EntryNode.TYPE_GAMEPLAY_RECRUITING:
                case EntryNode.TYPE_GAMEPLAY_STATUS:
                    generateGameplayPage(type);
                    break;
                case EntryNode.TYPE_MULTIPLAYER_COMMANDS:
                case EntryNode.TYPE_MULTIPLAYER_CREATE_GAME:
                case EntryNode.TYPE_MULTIPLAYER_JOIN_GAME:
                    generateMultiplayerPage(type);
                    break;
            }
        }
        content_page.layout();
        sp_content_page.layout();
        sp_content_page.setScrollPercentY(0);
    }

    private void generateRootPage(int type) {
        switch (type) {
            case RootNode.OVERVIEW:
                label_title.setText(Language.getText("WIKI_NODE_ROOT_" + type));
                Label label_p1 = new Label(Language.getText("WIKI_OVERVIEW_P1"), getContext().getSkin());
                label_p1.setWrap(true);
                content_page.add(label_p1).width(ts * 7).padBottom(ts / 8).row();
                break;
            case RootNode.ABOUT:
                label_title.setText(Language.getText("WIKI_NODE_ROOT_" + type));
                content_page.add(about_page);
                break;
            default:
                label_title.setText(Language.getText("WIKI_NODE_ROOT_" + type));
        }
    }

    private void generateGameplayPage(int type) {
        switch (type) {
            case EntryNode.TYPE_GAMEPLAY_ATTACKING:
                label_title.setText(Language.getText("WIKI_NODE_GAMEPLAY_ATTACKING"));
                break;
            case EntryNode.TYPE_GAMEPLAY_HEALING:
                label_title.setText(Language.getText("WIKI_NODE_GAMEPLAY_HEALING"));
                break;
            case EntryNode.TYPE_GAMEPLAY_INCOME:
                label_title.setText(Language.getText("WIKI_NODE_GAMEPLAY_INCOME"));
                break;
            case EntryNode.TYPE_GAMEPLAY_OBJECTIVES:
                label_title.setText(Language.getText("WIKI_NODE_GAMEPLAY_OBJECTIVES"));
                break;
            case EntryNode.TYPE_GAMEPLAY_RECRUITING:
                label_title.setText(Language.getText("WIKI_NODE_GAMEPLAY_RECRUITING"));
                break;
            case EntryNode.TYPE_GAMEPLAY_STATUS:
                label_title.setText(Language.getText("WIKI_NODE_GAMEPLAY_STATUS"));
                break;
        }
        gameplay_page.setType(type);
        content_page.add(gameplay_page);
    }

    private void generateTilePage(int index) {
        label_title.setText(Language.getText("LB_TILE_NUMBER") + index);
        tile_page.setIndex(index);
        content_page.add(tile_page);
    }

    private void generateUnitPage(int index) {
        label_title.setText(Language.getUnitName(index));
        unit_page.setIndex(index);
        content_page.add(unit_page);
    }

    private void generateAbilityPage(int ability) {
        label_title.setText(Language.getAbilityName(ability));
        ability_page.setAbility(ability);
        content_page.add(ability_page);
    }

    private void generateStatusPage(int status) {
        label_title.setText(Language.getStatusName(status));
        status_page.setStatus(status);
        content_page.add(status_page);
    }

    private void generateMultiplayerPage(int type) {
        switch (type) {
            case EntryNode.TYPE_MULTIPLAYER_COMMANDS:
                label_title.setText(Language.getText("WIKI_NODE_MULTIPLAYER_COMMANDS"));
                break;
            case EntryNode.TYPE_MULTIPLAYER_CREATE_GAME:
                label_title.setText(Language.getText("WIKI_NODE_MULTIPLAYER_CREATE_GAME"));
                break;
            case EntryNode.TYPE_MULTIPLAYER_JOIN_GAME:
                label_title.setText(Language.getText("WIKI_NODE_MULTIPLAYER_JOIN_GAME"));
                break;
        }
        multiplayer_page.setType(type);
        content_page.add(multiplayer_page);
    }

}
