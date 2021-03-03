package top.lizhistudio.autolua.conceal;

import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.InputEvent;

import java.lang.reflect.Method;

public final class InputManagerWrap {
    private static InputManager inputManager;
    private static Method injectInputEvent;
    private InputManagerWrap(){}

    static{
        try{
            Method getInstance = InputManager.class.getDeclaredMethod("getInstance");
            getInstance.setAccessible(true);
            inputManager =(InputManager)getInstance.invoke(null);
            injectInputEvent = InputManager.class.getDeclaredMethod(
                    "injectInputEvent", InputEvent.class,int.class);
        }catch(Exception e)
        {
            e.printStackTrace(System.out);
        }
    }

    public static boolean injectInputEvent(InputEvent inputEvent, int flags)
    {

        boolean result = false;
        try{
            result = (boolean)injectInputEvent.invoke(inputManager,inputEvent,flags);
        }catch(Exception e)
        {
            e.printStackTrace(System.out);
        }
        return result;
    }

    public static InputManager getInputManager()
    {
        return inputManager;
    }

    public static InputDevice getTouchDevice()
    {
        InputDevice inputDevice;
        for(int id:inputManager.getInputDeviceIds())
        {
            inputDevice = inputManager.getInputDevice(id);
            if((inputDevice.getSources()& InputDevice.SOURCE_TOUCHSCREEN)
                    == InputDevice.SOURCE_TOUCHSCREEN )
                return inputDevice;
        }
        return null;
    }

    public static InputDevice getKeyboardDevice()
    {
        InputDevice inputDevice;
        for(int id:inputManager.getInputDeviceIds())
        {
            inputDevice = inputManager.getInputDevice(id);
            if((inputDevice.getSources()& InputDevice.SOURCE_KEYBOARD)
                    == InputDevice.SOURCE_KEYBOARD)
                return inputDevice;
        }
        return null;
    }


    public static void printDeviceName()
    {
        InputDevice inputDevice;
        for(int id:inputManager.getInputDeviceIds())
        {
            inputDevice = inputManager.getInputDevice(id);
            System.out.println(inputDevice.getDescriptor());
        }
    }

    public static InputDevice getInputDevice(int id){
        return inputManager.getInputDevice(id);
    }
}
