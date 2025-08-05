package entity;

import java.util.Random;
import main.GamePanel;

// Representa o NPC "Old Man", herda da classe Entity
public class NPC_OldMan extends Entity {

    // Construtor: define direção inicial, velocidade e carrega os sprites
    public NPC_OldMan(GamePanel gp) {
        super(gp); // Chama o construtor da superclasse Entity

        direction = "down"; // Direção inicial
        speed = 1; // Velocidade fixa para o NPC

        getImage(); // Carrega as imagens de sprite para animação
        setDialogue(); // Define os diálogos do NPC
    }

    // Carrega as imagens do NPC em todas as direções (2 frames por direção)
    public void getImage() {
        up1 = setup("/resource/npc/oldman/oldman_up_1");
        up2 = setup("/resource/npc/oldman/oldman_up_2");
        down1 = setup("/resource/npc/oldman/oldman_down_1");
        down2 = setup("/resource/npc/oldman/oldman_down_2");
        left1 = setup("/resource/npc/oldman/oldman_left_1");
        left2 = setup("/resource/npc/oldman/oldman_left_2");
        right1 = setup("/resource/npc/oldman/oldman_right_1");
        right2 = setup("/resource/npc/oldman/oldman_right_2");
    }

    public void setDialogue() {
        dialogues[0] = "Olá, rapaz.";
        dialogues[1] = "Então você veio até esta ilha em busca do \ntesouro?";
        dialogues[2] = "Eu costumava ser um grande mago, mas agora... \nestou um pouco velho para aventuras.";
        dialogues[3] = "Bem, boa sorte para você!";
    }

    // Define o comportamento do NPC — neste caso, movimentação aleatória
    @Override
    public void setAction() {
        actionLockCounter++;

        // A cada 120 frames (2 segundos em 60 FPS), o NPC escolhe uma nova direção
        if (actionLockCounter == 120) {

            Random random = new Random();
            int i = random.nextInt(4); // Gera um número de 0 a 3

            // Altera a direção com base no número gerado
            switch (i) {
                case 0:
                    direction = "up";
                    break;
                case 1:
                    direction = "down";
                    break;
                case 2:
                    direction = "left";
                    break;
                case 3:
                    direction = "right";
                    break;
            }

            actionLockCounter = 0; // Reinicia o contador para o próximo intervalo
        }
    }

    public void speak() {

        super.speak(); // Chama o método speak da superclasse Entity
    }
}
