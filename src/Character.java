/**
 * Name: Samuel Xu, Edison Cai, Rocky Shi
 * Date: 06/09/2025
 * Description: The attributes of our Guess Who Characters
 */

/**
 * This is the class of our character. It includes all the attributes that we have, such as gender, eye colour and more.
 */
public class Character {
	private String name;
	private String gender;
	private String eyeColour;
	private String skinTone;
	private String hairColour;
	private boolean facialHair;
	private boolean glasses;
	private boolean visibilityOfTeeth;
	private boolean wearingOfHat;
	private String hairLength;
	private boolean piercings;
	private boolean visibility;

	// Our constructor, and we also have a value called visibility. This will tell us if the character is visible or not
	public Character (String defaultName, String defaultGender, String defaultEyeColour, String defaultSkinTone, String defaultHairColour, boolean defaultFacialHair, boolean defaultGlasses, boolean defaultVisibilityOfTeeth, boolean defaultWearingOfHat, String defaultHairLength, boolean defaultPiercings) {
		name = defaultName;
		gender = defaultGender;
		eyeColour = defaultEyeColour;
		skinTone = defaultSkinTone;
		hairColour = defaultHairColour;
		facialHair = defaultFacialHair;
		glasses = defaultGlasses;
		visibilityOfTeeth = defaultVisibilityOfTeeth;
		wearingOfHat = defaultWearingOfHat;
		hairLength = defaultHairLength;
		piercings = defaultPiercings;
		visibility = true;
	}

	// Getter methods
	public String getName() {
		return name;
	}
	public String getGender() {
		return gender;
	}
	public String getEyeColour() {
		return eyeColour;
	}
	public String getSkinTone() {
		return skinTone;
	}
	public String getHairColour() {
		return hairColour;
	}
	public boolean getFacialHair() {
		return facialHair;
	}
	public boolean getGlasses() {
		return glasses;
	}
	public boolean getVisibilityOfTeeth() {
		return visibilityOfTeeth;
	}
	public boolean getWearingOfHat() {
		return wearingOfHat;
	}
	public String getHairLength() {
		return hairLength;
	}
	public boolean getPiercings() {
		return piercings;
	}
	public boolean getVisibility() {
		return visibility;
	}

	//Setter methods
	public void toggleVisibility() {
		visibility = false;
	}
	public void resetVisibility() {
		visibility = true;
	}
}