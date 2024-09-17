/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Graphics2D;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Image;
/*     */ import java.awt.Insets;
/*     */ import java.awt.Paint;
/*     */ import java.awt.Rectangle;
/*     */ import javax.swing.JComponent;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JViewport;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class BackgroundPanel
/*     */   extends JPanel
/*     */ {
/*     */   public static final int SCALED = 0;
/*     */   public static final int TILED = 1;
/*     */   public static final int ACTUAL = 2;
/*     */   private Paint painter;
/*     */   private Image image;
/*  42 */   private int style = 0;
/*  43 */   private float alignmentX = 0.5F;
/*  44 */   private float alignmentY = 0.5F;
/*     */ 
/*     */   
/*     */   private boolean isTransparentAdd = true;
/*     */ 
/*     */   
/*     */   public BackgroundPanel(Image image) {
/*  51 */     this(image, 0);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public BackgroundPanel(Image image, int style) {
/*  58 */     setImage(image);
/*  59 */     setStyle(style);
/*  60 */     setLayout(new GridBagLayout());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public BackgroundPanel(Image image, int style, float alignmentX, float alignmentY) {
/*  67 */     setImage(image);
/*  68 */     setStyle(style);
/*  69 */     setImageAlignmentX(alignmentX);
/*  70 */     setImageAlignmentY(alignmentY);
/*  71 */     setLayout(new GridBagLayout());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public BackgroundPanel(Paint painter) {
/*  78 */     setPaint(painter);
/*  79 */     setLayout(new BorderLayout());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setImage(Image image) {
/*  86 */     this.image = image;
/*  87 */     repaint();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setStyle(int style) {
/*  94 */     this.style = style;
/*  95 */     repaint();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setPaint(Paint painter) {
/* 102 */     this.painter = painter;
/* 103 */     repaint();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setImageAlignmentX(float alignmentX) {
/* 110 */     this.alignmentX = (alignmentX > 1.0F) ? 1.0F : ((alignmentX < 0.0F) ? 0.0F : alignmentX);
/* 111 */     repaint();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setImageAlignmentY(float alignmentY) {
/* 118 */     this.alignmentY = (alignmentY > 1.0F) ? 1.0F : ((alignmentY < 0.0F) ? 0.0F : alignmentY);
/* 119 */     repaint();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void add(JComponent component) {
/* 126 */     add(component, (Object)null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Dimension getPreferredSize() {
/* 134 */     if (this.image == null) {
/* 135 */       return super.getPreferredSize();
/*     */     }
/* 137 */     return new Dimension(this.image.getWidth(null), this.image.getHeight(null));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void add(JComponent component, Object constraints) {
/* 145 */     if (this.isTransparentAdd) {
/* 146 */       makeComponentTransparent(component);
/*     */     }
/*     */     
/* 149 */     add(component, constraints);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setTransparentAdd(boolean isTransparentAdd) {
/* 158 */     this.isTransparentAdd = isTransparentAdd;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void makeComponentTransparent(JComponent component) {
/* 168 */     component.setOpaque(false);
/*     */     
/* 170 */     if (component instanceof JScrollPane) {
/* 171 */       JScrollPane scrollPane = (JScrollPane)component;
/* 172 */       JViewport viewport = scrollPane.getViewport();
/* 173 */       viewport.setOpaque(false);
/* 174 */       Component c = viewport.getView();
/*     */       
/* 176 */       if (c instanceof JComponent) {
/* 177 */         ((JComponent)c).setOpaque(false);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected void paintComponent(Graphics g) {
/* 187 */     super.paintComponent(g);
/*     */ 
/*     */ 
/*     */     
/* 191 */     if (this.painter != null) {
/* 192 */       Dimension d = getSize();
/* 193 */       Graphics2D g2 = (Graphics2D)g;
/* 194 */       g2.setPaint(this.painter);
/* 195 */       g2.fill(new Rectangle(0, 0, d.width, d.height));
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 200 */     if (this.image == null) {
/*     */       return;
/*     */     }
/*     */     
/* 204 */     switch (this.style) {
/*     */       case 0:
/* 206 */         drawScaled(g);
/*     */         return;
/*     */       
/*     */       case 1:
/* 210 */         drawTiled(g);
/*     */         return;
/*     */       
/*     */       case 2:
/* 214 */         drawActual(g);
/*     */         return;
/*     */     } 
/*     */     
/* 218 */     drawScaled(g);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void drawScaled(Graphics g) {
/* 226 */     Dimension d = getSize();
/* 227 */     g.drawImage(this.image, 0, 0, d.width, d.height, null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void drawTiled(Graphics g) {
/* 234 */     Dimension d = getSize();
/* 235 */     int width = this.image.getWidth(null);
/* 236 */     int height = this.image.getHeight(null);
/*     */     int x;
/* 238 */     for (x = 0; x < d.width; x += width) {
/* 239 */       int y; for (y = 0; y < d.height; y += height) {
/* 240 */         g.drawImage(this.image, x, y, null, null);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void drawActual(Graphics g) {
/* 251 */     Dimension d = getSize();
/* 252 */     Insets insets = getInsets();
/* 253 */     int width = d.width - insets.left - insets.right;
/* 254 */     int height = d.height - insets.top - insets.left;
/* 255 */     float x = (width - this.image.getWidth(null)) * this.alignmentX;
/* 256 */     float y = (height - this.image.getHeight(null)) * this.alignmentY;
/* 257 */     g.drawImage(this.image, (int)x + insets.left, (int)y + insets.top, this);
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\BackgroundPanel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */