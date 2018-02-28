const CryptoTwittos = artifacts.require("CryptoTwittos");

contract("CryptoTwittos", async accounts => {
  let id = 42;
  let initialPrice = 10000;

  it("should steal empty Twitto", async () => {
    let instance = await CryptoTwittos.deployed();
    instance.steal(id, initialPrice);
    let twitto = await instance.twittos(id);
    assert.equal(twitto[0], accounts[0]);
    assert.equal(twitto[1], initialPrice);
  });

  it("should steal existing Twitto", async () => {
    let newPrice = initialPrice + 10000;
    let instance = await CryptoTwittos.deployed();
    instance.steal(id, newPrice, { from: accounts[1], value: initialPrice });
    let twitto = await instance.twittos(id);
    assert.equal(twitto[0], accounts[1]);
    assert.equal(twitto[1], newPrice);
  });
});
