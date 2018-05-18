package simulator;

/**
 * 蜷悟喧蜃ｦ逅�繧ｯ繝ｩ繧ｹ
 * @author T.KASHIYAMA@IIS. UT.
 * @since 2014/07/31
 */
public class Assimilator {
	
	/** 蜷悟喧蜃ｦ逅�繧ｳ繝ｳ繝医Ο繝ｼ繝ｩ			*/	public IAController mController;
	
	/**
	 * 繧ｳ繝ｳ繧ｹ繝医Λ繧ｯ繧ｿ
	 * @param mController縲�蜷悟喧繧ｳ繝ｳ繝医Ο繝ｼ繝ｩ
	 */
	public Assimilator(IAController mController) {
		super();
		this.mController = mController;
	}
	
	/**
	 * 蜷悟喧螳溯｡�
	 * @return縲�result
	 */
	public int assimilate(){
		try{
			mController.initialize();
			while (mController.next()){}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
