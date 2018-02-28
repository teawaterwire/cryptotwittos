var CryptoTwittos = artifacts.require("./CryptoTwittos.sol");

module.exports = function(deployer) {
  deployer.deploy(CryptoTwittos);
};
