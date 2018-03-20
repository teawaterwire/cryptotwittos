pragma solidity ^0.4.19;

import 'zeppelin-solidity/contracts/ownership/Ownable.sol';
import 'zeppelin-solidity/contracts/lifecycle/Destructible.sol';
import 'zeppelin-solidity/contracts/lifecycle/Pausable.sol';
import 'zeppelin-solidity/contracts/math/SafeMath.sol';

contract CryptoTwittos is Ownable, Pausable, Destructible {
  using SafeMath for uint;

  // A Twitto is owned by a stealer and has a price
  struct Twitto {
    address stealer;
    uint price;
  }

  // Look up Twitto by ids
  mapping(uint => Twitto) public twittos;

  // All Twitto ids and counter
  uint[] public twittoIds;
  uint public twittosCounter;


  // Fire event when steal happens
  event stealEvent(
    uint indexed id,
    address indexed owner,
    uint price,
    address indexed stealer,
    uint newPrice
  );


  // Get twittoIds
  function getTwittoIds(bool all) public view returns (uint[]) {
    // Return empty array if counter is zero
    if (twittosCounter == 0) return new uint[](0);

    if (all) {
      // Return all of them
      return twittoIds;

    } else {
      // Create memory array to store filtered ids
      uint[] memory filteredIds = new uint[](twittosCounter);
      // Store number of belongings
      uint twittosCount = 0;

      for (uint i = 0; i < twittosCounter; i++) {
        // Check if stealer is sender
        if (twittos[twittoIds[i]].stealer == msg.sender) {
          filteredIds[twittosCount] = twittoIds[i];
          twittosCount++;
        }
      }

      // Copy the filteredIds array into a smaller array
      uint[] memory trophies = new uint[](twittosCount);
      for (uint j = 0; j < twittosCount; j++) {
        trophies[j] = filteredIds[j];
      }
      return trophies;
    }
  }

  // Steal a Twitto by paying its price and setting a new one
  function steal(uint id, uint256 newPrice) payable whenNotPaused public {

    // look up the twitto and put on storage
    Twitto storage _twitto = twittos[id];

    // Prevent self stealing!
    require(msg.sender != _twitto.stealer);

    // Make sure the sender pays the right price
    require(msg.value == _twitto.price);

    // Make sure that the new price is higher than the old price
    require(newPrice > _twitto.price);

    // Transfer value
    if (msg.value > 0) {
      _twitto.stealer.transfer(msg.value.mul(99).div(100));
    }

    // Push new Twitto if not existing
    if (_twitto.price == 0) {
      twittoIds.push(id);
      twittosCounter++;
    }

    // Trigger event
    stealEvent(id, _twitto.stealer, _twitto.price, msg.sender, newPrice);

    // Store new stealer
    _twitto.stealer = msg.sender;

    // Store new price
    _twitto.price = newPrice;

  }

  function withdraw() public onlyOwner {
    msg.sender.transfer(address(this).balance);
  }


}
