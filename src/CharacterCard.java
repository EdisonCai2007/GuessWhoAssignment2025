/**
 * Name: Samuel Xu, Edison Cai, Rocky Shi
 * Date: 06/09/2025
 * Description: Character Card UI; Used during gameplay
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.ButtonUI;

public class CharacterCard {
	private Character character;

	private boolean isVisible = true;

	
	public CharacterCard(Character defaultCharacter) {
		character = defaultCharacter;
	}

	/**
	 * Builds and returns a character card for a specific character. Includes the character's name and image. It also
	 * allows a feature where the player can toggle the character on and off, based on whether they are a viable guess.
	 *
	 * @return The JPanel of the character card
	 */
	public JPanel buildCharacterCardGUI() {

		// Get image path of character
		String imgPath = character.getName() + ".jpg";

		GridBagConstraints gbc = new GridBagConstraints();

		// Builds main character frame
		JPanel characterFrame = new JPanel();

		JButton characterButton = new JButton();
		characterButton.setBackground(Color.white);
		characterButton.setMargin(new Insets(0,0,0,0));

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridBagLayout());

		// Scale and build image
		ImageIcon img = new ImageIcon(new ImageIcon(imgPath).getImage().getScaledInstance(180, 150, java.awt.Image.SCALE_SMOOTH));
		JLabel imageLabel = new JLabel(img);

		gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0,0,20,0);

		innerPanel.add(imageLabel,gbc);

		JLabel nameLabel = new JLabel(character.getName().toUpperCase());
		nameLabel.setFont(new Font("Roboto", Font.BOLD, 32));
		gbc.gridy = 1;
        gbc.gridwidth = 1;

		innerPanel.add(nameLabel,gbc);

		// If the character card is clicked
		characterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isVisible = !isVisible; // Toggle visibility

				// Change UI based on visibile
				innerPanel.setBackground(isVisible ? Color.white : Color.gray);
				nameLabel.setEnabled(isVisible);
				imageLabel.setEnabled(isVisible);

				// Update GUI
				characterButton.revalidate();
				characterButton.repaint();
			}
		} );

		characterButton.add(innerPanel);
		characterFrame.add(characterButton);

		return characterFrame;
	}
}