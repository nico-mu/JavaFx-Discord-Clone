package de.uniks.stp.emote;

import javafx.scene.Node;
import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface LinkedImage {
    static <S> Codec<LinkedImage> codec() {
        return new Codec<LinkedImage>() {
            @Override
            public String getName() {
                return "LinkedImage";
            }

            @Override
            public void encode(DataOutputStream os, LinkedImage linkedImage) throws IOException {
                if (linkedImage.isReal()) {
                    os.writeBoolean(true);
                    String externalPath = (linkedImage.getEmoteName() + ".png").replace("\\", "/");
                    Codec.STRING_CODEC.encode(os, externalPath);
                } else {
                    os.writeBoolean(false);
                }
            }

            @Override
            public LinkedImage decode(DataInputStream is) throws IOException {
                if (is.readBoolean()) {
                    String imagePath = Codec.STRING_CODEC.decode(is);
                    imagePath = imagePath.replace("\\",  "/");
                    return new RealLinkedImage(imagePath);
                } else {
                    return new EmptyLinkedImage();
                }
            }
        };
    }

    boolean isReal();

    String getEmoteName();

    Node createNode();
}
