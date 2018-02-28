const CryptoTwittos = artifacts.require("CryptoTwittos");

contract("CryptoTwittos", async accounts => {
  let id = 42;
  let initialPrice = web3.toWei(1, "ether");

  it("should steal empty Twitto", async () => {
    let instance = await CryptoTwittos.deployed();
    await instance.steal(id, initialPrice);
    let twitto = await instance.twittos(id);
    assert.equal(twitto[0], accounts[0]);
    assert.equal(twitto[1], initialPrice);
  });

  it("should steal existing Twitto", async () => {
    let newPrice = initialPrice + web3.toWei(1, "ether");
    let instance = await CryptoTwittos.deployed();
    let coinbaseBalance = (await web3.eth.getBalance(accounts[0])).toNumber();
    await instance.steal(id, newPrice, {
      from: accounts[1],
      value: initialPrice
    });
    let coinbaseNewBalance = (await web3.eth.getBalance(
      accounts[0]
    )).toNumber();
    let twitto = await instance.twittos(id);
    assert.equal(coinbaseNewBalance - coinbaseBalance, initialPrice);
    assert.equal(twitto[0], accounts[1]);
    assert.equal(twitto[1].toNumber(), newPrice);
  });
});
