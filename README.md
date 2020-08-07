# Eclair Mobile

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Gitter chat](https://img.shields.io/badge/chat-on%20gitter-rose.svg)](https://gitter.im/ACINQ/eclair)

Eclair Mobile is a next generation, Lightning-ready Bitcoin wallet. It can be used as a regular Bitcoin wallet, and can also connect to the Lightning Network for cheap and instant payments.

This software is based upon [eclair](https://github.com/ACINQ/eclair), and follows the Lightning Network standard.

## Installation

The wallet is available on [Google Play](https://play.google.com/store/apps/details?id=fr.acinq.eclair.wallet), you can also download APKs from the [releases page](https://github.com/ACINQ/eclair-wallet/releases).

## Usage with Lightning

### Opening a LN channel

1. Make sure you have funds (swipe to the left from the home screen to display your address and receive funds).

2. Swipe to the right from the home screen, and click on the green `+` button.

3. You can now choose to scan/paste the adress of a Lightning Node.

   Alternatively, choose `Autoconnect` to initiate a connection with one of our nodes.

4. Enter the capacity of the channel and click `Open`.

   A transaction will be sent to fund the channel. You can find it in the transactions list as an outbound Bitcoin transaction, with an amount corresponding to the channel's desired capacity.
   At this point the channel will have a `WAIT_FOR_CONFIRMED` state and can not be used yet.

5. Once the channel reaches the `NORMAL` state (the funding transaction has 2+ confirmations) you can send payments!

### Sending a LN payment

1. Make sure you have at least one channel in a `NORMAL` state with enough balance.
2. In the Transaction view, click the Send button.

   You can now scan or paste a Lightning Payment request. This is an invoice generated by a node in the network, and which contains the necessary informations required to execute a LN payment.
   We have set up [Starblocks](https://starblocks.acinq.co), a virtual coffee shop for testers. You can use it to generate LN payment requests on testnet.

3. A window will open to display the informations about the payments. Click `Send Payment`.

   The wallet will now find a route from your node to the destination node. Depending on the topology of the network and the amount of hops needed to reach the destination node, you will pays fees. For now, this wallet does not enable you to limit the fees.
   Once a valid route is found, the balance of one of your channels will be updated.

   If no route can be found, the payment fails and your channels are unchanged. The reasons can be multiple:
   - the destination node is not online;
   - none of your channels has enough funds;
   - the nodes between you and the destination node do not have channels with sufficient capacity to relay your payment;
   - ...

### Receiving LN payments

1. Make sure you have at least one channel in a `NORMAL` state with enough receiving capacity.
2. Go to the `Settings` page and toggle `Enable receive over Lightning`.
3. Swipe to the left from the home screen and click the `LIGHTNING` tab.
4. A Lightning Payment request is displayed; it can be paid from any Lightning-enabled wallet.

NB: when you enable receiving over Lightning, you must be aware of a couple limitations:

- Your phone needs to regularly have access to the internet to monitor the blockchain, otherwise your funds may be at risk. Eclair-mobile runs a background task that will check the blockchain, even if you don't launch the app daily.
- Your phone needs to be online with the app open to receive a payment.

See [here](https://medium.com/@ACINQ/enabling-receive-on-eclair-mobile-2e1b87bd1e3a) for more thorough explanations.

### Closing a channel

1. In the LN channels list, click on the channel you want to close.
2. Click on the `Close channel` button

   If the channel is not in a `NORMAL` state, the closing will be uncooperative. It means that you will have to wait for 144 blocks to receive your funds. This is a Lightning Network specification to prevent theft.

3. You will receive a Bitcoin transaction with the leftover balance of the channel.

## Developers

1. clone this project
2. clone [eclair](https://github.com/ACINQ/eclair) and checkout the `android` branch.

   Follow the steps [here](https://github.com/ACINQ/eclair/blob/android/BUILD.md) to build the eclair-core library.

3. Open the Eclair Mobile project with Android studio. You should now be able to install it on your phone/on an emulator.
