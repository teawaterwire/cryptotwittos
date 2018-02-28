const CryptoTwittos = artifacts.require("CryptoTwittos");

contract("CryptoTwittos", accounts => {
  let id = 42;
  let initialPrice = 10000;

  it("should fail if newPrice is lower", async () => {
    let instance = await CryptoTwittos.deployed();
    await instance.steal(id, initialPrice);
    let newPrice = initialPrice - 3;
    try {
      await instance.steal(id, newPrice, {
        from: accounts[1],
        value: initialPrice
      });
      assert.fail();
    } catch (error) {
      assert(
        error.message.indexOf("error") >= 0,
        "error message must contain error"
      );
    }
  });
});
