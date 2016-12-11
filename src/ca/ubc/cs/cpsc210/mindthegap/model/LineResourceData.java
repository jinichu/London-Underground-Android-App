package ca.ubc.cs.cpsc210.mindthegap.model;

/**
 * Represents tube line resource data (file resource name that contains
 * line data and and integer that represents ARGB colour used by TfL
 * for signage on this line)
 */
public enum LineResourceData {
    CENTRAL("central_inbound.json",  0xFFDC241F),
    NORTHERN("northern_inbound.json", 0xFF000000),
    PICCADILLY("piccadilly_inbound.json", 0xFF0019A8),
    VICTORIA("victoria_inbound.json", 0xFF00A0E2),
    BAKERLOO("bakerloo_inbound.json", 0xFF894E24),
    DISTRICT("district_inbound.json", 0xFF007229),
    JUBILEE("jubilee_inbound.json", 0xFF868F98);


    private String fileName;
    private int colour;

    LineResourceData(String fileName, int colour) {
        this.fileName = fileName;
        this.colour = colour;
    }

    public String getFileName() {
        return fileName;
    }

    public int getColour() {
        return colour;
    }
}
