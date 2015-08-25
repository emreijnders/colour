package emr.colour.generators;

public enum Generators
{
	LINES , CIRCLES , CLOUD , CORAL , TREE;
	
	public static ImageGenerator getGenerator( Generators type )
	{
		ImageGenerator generator;
		switch( type )
		{
			case LINES:
				generator = new ImageGeneratorLines();
				break;
			case CIRCLES:
				generator = new ImageGeneratorCircles();
				break;
			case CLOUD:
				generator = new ImageGeneratorCloud();
				break;
			case CORAL:
				generator = new ImageGeneratorCoral();
				break;
			case TREE:
				generator = new ImageGeneratorTree();
				break;
			default:
				generator = null;
		}
		return generator;
	}
}