package PTdata;

	public enum ETransport {
		/**	移動モード滞在	*/	STAY(99),
		/**	移動モード徒歩	*/	WALK(1),
		/**	移動モード車	*/	VEHICLE(2),
		/**	移動モード列車	*/	TRAIN(3);
		
		private final int id;

		private ETransport(int id){
			this.id = id;
		}
		public int getId(){
			return this.id;
		}
		public static ETransport valueOf(int id){
			for (ETransport num : values()){
				if (num.getId() == id){
					return num;
				}
			}
			return null;
		}
	}

