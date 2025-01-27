package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.client.ClientInvoiceManager;

@Environment(EnvType.CLIENT)
public class PayInvoiceDetailsScreen extends Screen {
    private final String invoice;
    private final Screen previousScreen;

    public PayInvoiceDetailsScreen(String invoice, Screen previousScreen) {
        super(Text.literal("Detalles de Factura"));
        this.invoice = invoice;
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = Math.max(20, (int) (this.height * 0.3));

        // 🔍 Depuración
        System.out.println("📜 Abriendo detalles de factura: " + invoice);

        // Botón para pagar la factura
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Pagar"), button -> payInvoice())
                .dimensions(centerX - 50, startY + 80, 100, 20)
                .build());

        // Botón para volver atrás
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Volver"), button -> this.client.setScreen(previousScreen))
                .dimensions(centerX - 50, startY + 110, 100, 20)
                .build());
    }

    private void payInvoice() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(invoice);
        ClientPlayNetworking.send(new Identifier("dragoneconomy", "pay_invoice"), buf);

        // ✅ Eliminar la factura del cliente después de pagarla
        ClientInvoiceManager.removeInvoice(invoice);

        this.client.setScreen(null);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = this.width / 2;
        int startY = Math.max(20, (int) (this.height * 0.3));

        context.drawText(this.textRenderer, Text.literal("Detalles de Factura").formatted(Formatting.GOLD), centerX - 40, startY - 30, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Factura: " + invoice), centerX - 75, startY, 0xFFFFFF, false);

        super.render(context, mouseX, mouseY, delta);
    }
}
