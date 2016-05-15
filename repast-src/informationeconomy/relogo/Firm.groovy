/**
 * 
 */
package informationeconomy.relogo

import static repast.simphony.relogo.Utility.*;
import static repast.simphony.relogo.UtilityG.*;
import repast.simphony.relogo.Plural;
import repast.simphony.relogo.Stop;
import repast.simphony.relogo.Utility;
import repast.simphony.relogo.UtilityG;
import repast.simphony.relogo.schedule.Go;
import repast.simphony.relogo.schedule.Setup;
import informationeconomy.ReLogoTurtle;

/*
 * based on 
 */
class Firm extends ReLogoTurtle {

	def initialFood = 0
	def initialData = 0
	def initialGold = 0
	
	def currentFood = 0
	def currentData = 0
	def currentGold = 0
	
	def actions = []
	def activity = []
	
	def foodPerStep = 0
	def dataPerStep = 0
	def goldPerStep = 0
	
	def step() {
		def utilityMakingFood = utility(currentFood + foodPerStep, currentData, currentGold)
		def utilityMakingData = utility(currentFood, currentData + dataPerStep, currentGold)
		def utilityMakingGold = utility(currentFood, currentData, currentGold + goldPerStep)

		// work out the mrs for the firm
		def mrsFood = mrsFood()
		def mrsData = mrsData()

		// set up a variable to record the best trade found
		def trade = [
			firm: null,
			price: 0,
			food: currentFood,
			data: currentData,
			gold: currentGold,
			utility: currentUtility()
		]

		// cycle through each of the firms to see whether a trade is worthwhile
		Firm thisFirm = self()
		firms().each {
			def resultTradingFood = null
			if (mrsFood >= 1 && it.mrsFood() < 1) {
				// more GOLD than FOOD, so buy FOOD
				resultTradingFood = thisFirm.tryBuyingFood(it)
			} else if (mrsFood < 1 && it.mrsFood() >= 1) {
				// more FOOD than GOLD, so sell FOOD
				resultTradingFood = thisFirm.trySellingFood(it)
			} else {
				resultTradingFood = trade
			}
			def resultTradingData = null
			if (mrsData >= 1 && it.mrsData() < 1) {
				// more GOLD than DATA, so buy DATA
				resultTradingData = thisFirm.tryBuyingData(it)
			} else if (mrsData < 1 && it.mrsData() >= 1) {
				// more DATA than GOLD, so sell DATA
				resultTradingData = thisFirm.trySellingData(it)
			} else {
				resultTradingData = trade
			}

			// set the best trade to the result if it's a better trade than the best found so far
			if (resultTradingFood['utility'] > trade['utility']) {
				if (resultTradingData['utility'] > resultTradingFood['utility']) {
					trade = resultTradingData
				} else {
					trade = resultTradingFood
				}
			} else if (resultTradingData['utility'] > trade['utility']) {
				trade = resultTradingData
			}
		}
		
		def action = []
		def randomlyTrue = random(10000) > 5000
		def produce = [
			type: 'make',
			good: 'gold',
			amount: goldPerStep,
			utility: utilityMakingGold
		]
		if (utilityMakingFood > produce['utility'] || (utilityMakingFood == produce['utility'] && randomlyTrue)) {
			produce = [
				type: 'make',
				good: 'food',
				amount: foodPerStep,
				utility: utilityMakingFood
			]
		}
		if (utilityMakingData > produce['utility'] || (utilityMakingData == produce['utility'] && randomlyTrue)) {
			produce = [
				type: 'make',
				good: 'data',
				amount: dataPerStep,
				utility: utilityMakingData
			]
		}
		
		if (trade['firm'] != null && (trade['utility'] > produce['utility'] || (trade['utility'] == produce['utility'] && randomlyTrue))) {
			action = makeTrade(trade)
		} else {
			action = makeProduce(produce)
		}
		actions << action
		activity << action
	}
	
	def utility(food, data, gold) {
		food * data * gold
	}

	def currentUtility() {
		utility(currentFood, currentData, currentGold)
	}
	
	def mrsFood() {
		currentGold / currentFood
	}
	
	def priceForFoodTrade(Firm firm) {
		(currentGold + firm.currentGold) / (currentFood + firm.currentFood)
	}
	
	def mrsData() {
		currentGold / currentData
	}
	
	def priceForDataTrade(Firm firm) {
		(currentGold + firm.currentGold) / (currentData + firm.currentData)
	}

	def tryBuyingFood(Firm firm) {
		def price = priceForFoodTrade(firm)
		def foodToBuy = Math.floor((firm.currentFood - currentFood) / 2)
		def result = [
			firm: firm,
			price: price,
			food: currentFood + foodToBuy,
			data: currentData,
			gold: currentGold - foodToBuy * price,
			utility: 0
		]
		result.put('utility', utility(result['food'], result['data'], result['gold']))
		return result
	}
	
	def trySellingFood(Firm firm) {
		def price = priceForFoodTrade(firm)
		def foodToSell = Math.floor((currentFood - firm.currentFood) / 2)
		def result = [
			firm: firm,
			price: price,
			food: currentFood - foodToSell,
			data: currentData,
			gold: currentGold + foodToSell * price,
			utility: 0
		]
		result.put('utility', utility(result['food'], result['data'], result['gold']))
		return result
	}
	
	def tryBuyingData(Firm firm) {
		def price = priceForDataTrade(firm)
		def dataToBuy = Math.floor((firm.currentData - currentData) / 2)
		def result = [
			firm: firm,
			price: price,
			food: currentFood,
			data: currentData + dataToBuy,
			gold: currentGold - dataToBuy * price,
			utility: 0
		]
		result.put('utility', utility(result['food'], result['data'], result['gold']))
		return result
	}
	
	def trySellingData(Firm firm) {
		def price = priceForDataTrade(firm)
		def dataToSell = Math.floor((currentData - firm.currentData) / 2)
		def result = [
			firm: firm,
			price: price,
			food: currentFood,
			data: currentData, // comment this line out for a COAL-FOOD-GOLD model and in for a DATA-FOOD-GOLD model
			// data: currentData - dataToSell, // comment this line in for a COAL-FOOD-GOLD model and out for a DATA-FOOD-GOLD model
			gold: currentGold + dataToSell * price,
			utility: 0
		]
		result.put('utility', utility(result['food'], result['data'], result['gold']))
		result.put('data', currentData - dataToSell) // comment this line out for a COAL-FOOD-GOLD model and in for a DATA-FOOD-GOLD model
		return result
	}

	def makeTrade(trade) {
		def action = [ type: 'trade', food: trade['food'] - currentFood, data: trade['data'] - currentData, gold: trade['gold'] - currentGold, price: trade['price'], utility: trade['utility'] ]
		Firm firm = trade['firm']
		firm.currentFood += currentFood - trade['food']
		firm.currentData += currentData > trade['data'] ? currentData - trade['data'] : 0 // comment this line out for a COAL-FOOD-GOLD model and in for a DATA-FOOD-GOLD model
		// firm.currentData += currentData - trade['data'] // comment this line in for a COAL-FOOD-GOLD model and out for a DATA-FOOD-GOLD model
		firm.currentGold += currentGold - trade['gold']
		firm.activity << [ type: 'receive-trade', food: currentFood - trade['food'], data: currentData - trade['data'], gold: currentGold - trade['gold'], price: trade['price'], utility: firm.currentUtility() ]
		currentFood = trade['food']
		currentData = trade['data'] > currentData ? trade['data'] : currentData
		currentGold = trade['gold']
		return action
	}
	
	def makeProduce(produce) {
		if (produce['good'] == 'food') {
			currentFood += produce['amount']
		} else if (produce['good'] == 'data') {
			currentData += produce['amount']
		} else {
			currentGold += produce['amount']
		}
		return produce
	}
	
}
