pragma solidity ^0.4.19;


contract CryptoTwittos {


  // A Twitto is owned by a stealer and has a price
  struct Twitto {
    address stealer;
    uint price;
  }

  // Look up Twitto by ids
  mapping(uint => Twitto) public twittos;

  // All Twitto ids and counter
  uint[] public twittoIds;
  uint twittosCounter;


  // Get all twittoIds
  function getTwittoIds() public view returns (uint[]) {
    /* if (twittosCounter == 0) return new uint[](0); */
    /* uint[] memory ids = new uint[](twittosCounter); */
    return twittoIds;
  }

  // Steal a Twitto by paying its price and setting a new one
  function steal(uint id, uint256 newPrice) payable public {

    // look up the twitto and put on storage
    Twitto storage _twitto = twittos[id];

    // Prevent self stealing!
    require(msg.sender != _twitto.stealer);

    // Make sure the sender pays the right price
    require(msg.value == _twitto.price);

    // Make sure that the new price is higher than the old price
    require(newPrice > _twitto.price);

    // Transfer value or push new id
    if (msg.value > 0) {
      _twitto.stealer.transfer(msg.value);
    } else {
      twittoIds.push(id);
      twittosCounter++;
    }

    // Store new stealer
    _twitto.stealer = msg.sender;

    // Store new price
    _twitto.price = newPrice;

  }


}
