#!/usr/bin/python
from bluepy.btle import *
import sys
import thread

def scan():
    sc = Scanner()
    result = sc.scan()


class MyDelegate(DefaultDelegate):
    def __init__(self, params):
        DefaultDelegate.__init__(self)
        
    def handleNotification(self, cHandle, data):
        sys.stdout.write(data)
        
def asyncRead(i,o):
    pass

        
def connect(destination='ea:a4:7b:62:1c:a6',addrType='random'):
    per = Peripheral(destination,addrType)
    per.setDelegate(MyDelegate(None))
    services = per.getServices()

    #uartService = services[4]
    uartService = per.getServiceByUUID('6E400001-B5A3-F393-E0A9-E50E24DCCA9E')
    
    #txCharacteristic = uartService.getCharacteristics()[1]
    #rxCharacteristic = uartService.getCharacteristics()[0]
    txCharacteristic = uartService.getCharacteristics('6E400002-B5A3-F393-E0A9-E50E24DCCA9E')[0]
    rxCharacteristic = uartService.getCharacteristics('6E400003-B5A3-F393-E0A9-E50E24DCCA9E')[0]
    cccid = AssignedNumbers.client_characteristic_configuration    
    
    desc = per.getDescriptors(uartService.hndStart,uartService.hndEnd)
    d, = [d for d in desc if d.uuid==cccid]
    per.writeCharacteristic(d.handle, '\1\0')
            

    running = True
    try:
        while running:
			r,s,e = select.select([sys.stdin],[],[],0.1)
			for re in r:
				txCharacteristic.write(re.read(1))
            
    except:
        running = False

    finally:
        per.disconnect()
    
if __name__ == "__main__":
    connect()
