package dev.fixpot47.joinbell;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class JoinBellScreen extends Screen {
    private final Screen parent;
    private EditBox playerNameBox;
    private int page;
    private int rowsPerPage;

    public JoinBellScreen(Screen parent) {
        super(Component.translatable("joinbell.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int inputY = 48;

        this.playerNameBox = new EditBox(
                this.font,
                centerX - 110,
                inputY,
                160,
                20,
                Component.translatable("joinbell.input")
        );
        this.playerNameBox.setMaxLength(16);
        this.addRenderableWidget(this.playerNameBox);

        this.addRenderableWidget(Button.builder(Component.translatable("joinbell.add"), button -> {
            if (JoinBellConfig.get().addPlayer(this.playerNameBox.getValue())) {
                this.playerNameBox.setValue("");
                this.page = 0;
                this.rebuildWidgets();
            }
        }).bounds(centerX + 56, inputY, 64, 20).build());

        List<String> players = JoinBellConfig.get().trackedPlayers();
        this.rowsPerPage = Math.max(3, Math.min(7, (this.height - 135) / 24));
        int pageCount = getPageCount(players.size());
        if (this.page >= pageCount) {
            this.page = Math.max(0, pageCount - 1);
        }

        int left = centerX - 140;
        int startY = 82;
        int startIndex = this.page * this.rowsPerPage;
        int endIndex = Math.min(players.size(), startIndex + this.rowsPerPage);

        for (int i = startIndex; i < endIndex; i++) {
            String player = players.get(i);
            int y = startY + (i - startIndex) * 24;

            Button name = Button.builder(Component.literal(player), button -> {
            }).bounds(left, y, 205, 20).build();
            name.active = false;
            this.addRenderableWidget(name);

            this.addRenderableWidget(Button.builder(Component.translatable("joinbell.remove"), button -> {
                JoinBellConfig.get().removePlayer(player);
                this.rebuildWidgets();
            }).bounds(left + 211, y, 69, 20).build());
        }

        int footerY = this.height - 28;
        Button previous = Button.builder(Component.literal("<"), button -> {
            this.page--;
            this.rebuildWidgets();
        }).bounds(centerX - 140, footerY, 40, 20).build();
        previous.active = this.page > 0;
        this.addRenderableWidget(previous);

        this.addRenderableWidget(Button.builder(Component.translatable("joinbell.done"), button -> this.onClose())
                .bounds(centerX - 50, footerY, 100, 20).build());

        Button next = Button.builder(Component.literal(">"), button -> {
            this.page++;
            this.rebuildWidgets();
        }).bounds(centerX + 100, footerY, 40, 20).build();
        next.active = this.page + 1 < pageCount;
        this.addRenderableWidget(next);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(this.font, this.title, this.width / 2, 16, -1);
        graphics.centeredText(
                this.font,
                Component.translatable("joinbell.screen.hint"),
                this.width / 2,
                31,
                -8355712
        );

        if (JoinBellConfig.get().trackedPlayers().isEmpty()) {
            graphics.centeredText(
                    this.font,
                    Component.translatable("joinbell.empty"),
                    this.width / 2,
                    93,
                    -8355712
            );
        }

        int pageCount = getPageCount(JoinBellConfig.get().trackedPlayers().size());
        if (pageCount > 1) {
            graphics.centeredText(
                    this.font,
                    Component.literal((this.page + 1) + " / " + pageCount),
                    this.width / 2,
                    this.height - 43,
                    -8355712
            );
        }
    }

    @Override
    public void onClose() {
        JoinBellConfig.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    private int getPageCount(int count) {
        if (this.rowsPerPage <= 0) {
            return 1;
        }
        return Math.max(1, (count + this.rowsPerPage - 1) / this.rowsPerPage);
    }
}
