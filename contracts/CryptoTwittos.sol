pragma solidity ^0.4.17;


contract CryptoTwittos {


  // A Twitto is owned by a stealer and has a price
  struct Twitto {
    address stealer;
    uint price;
  }

  // Look up Twitto by ids
  mapping(uint => Twitto) public twittos;


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

    // Transfer value
    if (msg.value > 0) _twitto.stealer.transfer(msg.value);

    // Store new stealer
    _twitto.stealer = msg.sender;

    // Store new price
    _twitto.price = newPrice;

  }


}
