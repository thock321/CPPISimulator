import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A naive Constant Proportion Portfolio Insurance (CPPI) simulator that uses the daily closing price.  It is designed to read from a .csv file with historical data downloaded from Yahoo! Finance.
 * It will simulate from the start to the end of the .csv file.  So if you want to run the simulation on the last 5 years of the S&P 500, you would download the 5 year historical data from Yahoo! Finance and use that as the input file.
 * More info on CPPI can be found here: <url>https://www.investopedia.com/terms/c/cppi.asp</url>
 */
public class CPPISimulator {

    private double portfolio, floor, maxLoss, interest;
    private List<Double> prices;//Each index represents a tick, and the value is the price at that tick
    //e.g. prices[0] = 200 means the price is 200 at tick 0.

    /**
     * Create a new <code>CPPISimulator</code> instance.
     * @param prices List of daily close prices.
     * @param portfolio The starting portfolio value in USD.
     * @param floor The minimum portfolio value we are willing to accept in USD.
     * @param maxLoss The maximum loss we predict could occur as a decimal.  For example, if we think that the S&P 500 might crash 20% at most, then our maximum loss is 0.2.
     * @param interest The interest rate of the safety account.  An online high-yield savings account is usually around 2%.
     */
    public CPPISimulator(List<Double> prices, double portfolio, double floor, double maxLoss, double interest) {
        this.portfolio = portfolio;
        this.floor = floor;
        this.maxLoss = maxLoss;
        this.prices = prices;
        this.interest = interest;
    }

    /**
     * Get the CPPI multiplier, which is the inverse of the maximum crash possibility.
     * @return The multiplier.  Infinity if <code>maxLoss</code> is 0, meaning we invest all of our portfolio into the risky asset.
     */
    public double getMultiplier() {
        return 1.0 / maxLoss;
    }


    /**
     * Simulates CPPI.
     * We always round down dollar values to 2 decimal places.
     */
    public void simulate() {
        double startPortfolio = portfolio;
        if (prices.size() < 2)
            return;
        //We recalculate how much $ to invest into risky asset vs safe asset every day.
        for (int i = 1; i < prices.size(); i++) {
            double currPrice = prices.get(i - 1);
            double nextPrice = prices.get(i);
            //This is how much $ we allocate into the risky asset
            double allocated = getMultiplier() * (portfolio - floor);
            allocated = Math.min(allocated, portfolio);
            //Calculate how much of the risky asset to buy
            double numAllocated = (long) Math.floor(allocated / currPrice);
            //Calculate current value of risky asset
            allocated = numAllocated * currPrice;
            allocated = Math.floor(allocated * 100.0) / 100;

            //This is how much $ we allocate into the safe asset
            double remaining = (portfolio - allocated);
            remaining *= 1 + (interest / 365.0);//1.00005479452054794520547945205479452;
            //Round down to 2 decimal places
            remaining = Math.floor(remaining * 100.0) / 100;

            //Calculate new value of risky asset
            double percentChange = nextPrice / currPrice;
            allocated = numAllocated * nextPrice;
            allocated = Math.floor(allocated * 100.0) / 100;

            portfolio = allocated + remaining;
            portfolio = Math.floor(portfolio * 100.0) / 100;
            System.out.println(portfolio);
            //Check if the portfolio has reached the floor value.
            if (portfolio <= floor) {
                System.out.println("Floor reached, exitting position");
                //System.out.println(prices.get(i));
                break;//Exit position
            }
        }
        //Print out the nominal dollar value of the portfolio.
        System.out.println("Portfolio: $" + portfolio);
        System.out.println("Return: " + ((portfolio / startPortfolio - 1) * 100) + "%");
    }

    public static void main(String[] args) {
        try {
            Scanner s = new Scanner(new File("./^GSPC.csv"));
            s.useDelimiter(",|\\n");
            //skip the header
            for (int i = 0;  i < 7; i++)
                s.next();
            List<Double> priceList = new ArrayList<>();
            while (s.hasNext()) {
                //Get daily close price by skipping the first 4 columns
                for (int i = 0;  i < 4; i++)
                    s.next();
                priceList.add(Double.parseDouble(s.next()));
                s.next();
                s.next();
            }
            new CPPISimulator(priceList, 100000, 80000, 0.2, 0.02).simulate();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}