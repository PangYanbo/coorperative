package simulator;

/**
 * 蜷悟喧繧ｳ繝ｳ繝医Ο繝ｼ繝ｩ
 * @author T.KASHIYAMA@IIS. UT.
 * @since 2014/07/31
 */
public interface IAController
{
	/**
	 * 繧､繝九す繝｣繝ｩ繧､繧ｺ
	 * @return result
	 */
	public int initialize();
	
	/**
	 * 荳�譌･蛻�縺ｮ繝医Μ繝�繝励ｒ隱ｭ縺ｿ霎ｼ縺ｿ
	 * @return result
	 */
	public boolean next();
}
