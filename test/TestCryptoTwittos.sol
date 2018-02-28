pragma solidity ^0.4.17;


import "truffle/Assert.sol";
import "truffle/DeployedAddresses.sol";
import "../contracts/CryptoTwittos.sol";


contract TestCryptoTwittos {

  CryptoTwittos cryptoTwittos = CryptoTwittos(DeployedAddresses.CryptoTwittos());

  uint initialPrice = 1000;
  uint id = 42;
  address expectedStealer = this;

  function testSteal() public {
    uint price;
    address stealer;
    cryptoTwittos.steal(id, initialPrice);
    (stealer, price) = cryptoTwittos.twittos(id);

    Assert.equal(stealer, expectedStealer, "New stealer should be the test contract.");
    Assert.equal(price, initialPrice, "New price of stolen Twitto should be recorded.");
  }


}


contract TestCryptoTwittosExisting {

  CryptoTwittos cryptoTwittos = CryptoTwittos(DeployedAddresses.CryptoTwittos());

  uint initialPrice = 1000;
  uint id = 42;
  address expectedStealer = this;

  function testStealExisting() public {
    uint newPrice = initialPrice + 1000;
    cryptoTwittos.steal(id, newPrice);
    uint price;
    address stealer;
    (stealer, price) = cryptoTwittos.twittos(id);

    Assert.equal(stealer, expectedStealer, "There should be a new Stealer");
    Assert.equal(price, newPrice, "There should be a new price");
  }

}
