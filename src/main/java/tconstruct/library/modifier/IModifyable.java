package tconstruct.library.modifier;

public interface IModifyable {

    /**
     * @return The base tag to modify. Ex: InfiTool
     */
    String getBaseTagName();

    String getModifyType();

    String[] getTraits();
}
