package informationeconomy.relogo

import static repast.simphony.relogo.Utility.*;
import static repast.simphony.relogo.UtilityG.*;
import repast.simphony.relogo.Stop;
import repast.simphony.relogo.Utility;
import repast.simphony.relogo.UtilityG;
import repast.simphony.relogo.schedule.Go;
import repast.simphony.relogo.schedule.Setup;
import informationeconomy.ReLogoObserver;

class UserObserver extends ReLogoObserver{

	@Setup
	def setup(){
		clearAll()
		setDefaultShape(Firm, "circle")
		createFirms(500){
			setxy(randomXcor(),randomYcor())
			foodPerStep = random(29) + 1
			goldPerStep = random(29) + 1
			currentFood = foodPerStep
			currentGold = goldPerStep
		}
	}
	
	@Go
	def go(){
		ask(firms()){
			step()
		}
	}

}