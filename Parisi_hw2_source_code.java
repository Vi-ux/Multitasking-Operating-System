/*
 *Authors: Vito Parisi, Matthew Joseph, Eddy Olu
 * ID: 301018916 Vito
 * ID: 300941030 Matt
 * ID: 301015951 Eddy
 * HW: #2
 * Date: 04-13-2022
 *
 * Task description
 * The (MTOPS) is a Multi-Tasking Operating System
 * This machine is used to simulation memory allocation and free system calls, character-oriented input and output system calls
 * processes creation and termination, stack operations, run Null system process when there is no other process in the Ready Queue
 * process scheduling using Priority Round Robin algorithm for short term process scheduler for a timesharing operating system
 * maintain User Mode and Operating System Mode in the PSR when necessity, handle user commands and resulting interrupt handling
 * handling the following interrupts: (0) no interrupt, (1) run program, (2) shutdown, (3) read character (input), and (4) print character (output)
 *
 *
 *
 *
 *
 */


import java.io.*;
import java.util.*;


public class Parisi_hw2_source_code
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Hardware variables
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static long[] gpr;//initialized gprs.
    static long[] memory;//initialized main memory array.
    static long MAR, MBR, CLOCK, IR, PSR, PC, SP; //initialized hardware components.
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //const
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static final long START_ADDRESS_OF_USER_PROGRAM_AREA = 0; //const represents the start of user program area
    static final long ADDRESS_RANGE_CAP = 2999; //const represents the end address of the user program area
    static final long START_ADDRESS_OF_USER_FREE_LIST = 3000; //const represents the starting address of the user free list
    static final long END_ADDRESS_OF_USER_FREE_LIST = 6999; //const represents the address of the user free list
    static final long START_ADDRESS_OF_OS_FREE_LIST = 7000;////Holds the starting address of the OS free list.
    static final long END_ADDRESS_OF_OS_FREE_LIST = 10000;////Holds the starting address of the OS free list.
    static final long MAX_MEMORY_ADDRESS = 9999; //const represents the highest possible memory address
    static final long STACK_START_ADDRESS_INDEX = 5; //const represents the Stack Starting Address of a PCB
    static final long STACK_SIZE = 9; //const used to check the stack size
    static final long EndOfMachLangProgram = -1; //variable used to tell then the program file ends
    static final long EndOfList = -1;//used for push and pop
    static final int OK = 0; //const used to represent 0
    static final int NoInterrupt = 0;  //used for user to interact with machine to declare no interrupt
    static final int RunProgramInterrupt = 1;  //used for user to interact with machine to declare a run program interrupt
    static final int ShutdownSystemInterrupt = 2;  //used for user to interact with machine to declare a system shutdown
    static final int IO_GETCInterrupt = 3;  //used for user to interact with machine to declare a GETC interrupt
    static final int IO_PUTCInterrupt = 4;  //used for user to interact with machine to declare a PUTC interrupt
    static final int Process_Create = 1;  //var for system call function to create process
    static final int Process_Delete = 2;  //var for system call function to delete process
    static final int Process_Inquiry = 3;  //var for system call function for process inquiry
    static final int Mem_Alloc = 4;  //var for system call function to declare a memory allocation
    static final int Mem_Free = 5;  //var for system call function to declare a block of memory to free
    static final int Msg_Send = 6;  //var for system call function to send message
    static final int Msg_Receive = 7;  //var for system call function to recieve message
    static final int IO_GETC = 8;  //var for system call function to create a GETC interrupt
    static final int IO_PUTC = 9;  //var for system call function to create a PUTC interrupt
    static final int Time_Get = 10;  //var for system call function to get time
    static final int Time_Set = 11;  //var for system call function to set time
    static final long HaltInProgramReached = 1;  //used to declare halt in program has been reached
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Error codes
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static final int INVALID_PC_VALUE_ERROR = -2; //Invalid PC value
    static final int INVALID_MEMORY_RANGE_ERROR = -3; //Memory out of range
    static final int NO_END_OF_PROGRAM_INDICATOR_ERROR = -4; //End of program not found
    static final int OPENING_FILE_ERROR = -5; //Error opening file
    static final int INVALID_ADDRESS_IN_GPR_ERROR = -6; //address in GPR is invalid
    static final int INVALID_MODE_ERROR = -7; //used when a gpr is in a invalid mode
    static final int INVALID_OPERAND_GPR_ERROR = -8; //gpr value is invalid
    static final int DIVIDE_BY_ZERO_ERROR = -9; //divide by zero error
    static final int STACK_OVERFLOW_ERROR = -10; //stack overflow error
    static final int STACK_UNDERFLOW_ERROR = -11; //stack underflow error
    static final int INVALID_OP_CODE_ERROR = -12; //OpCode is invalid
    static final int UNKNOWN_PROGRAM_ERROR = -13; //unknown programming error
    static final long ERROR_NO_MEM = -14;  //error code no memory in allocation
    static final long ERROR_TOO_SMALL_MEM = -15;  //error code memory size too small
    static final long ERROR_NO_MEM_BLOCK = -16;  //error code no memory block
    static final long REQUESTED_MEMORY_TOO_SMALL = -17;  //error code requested memory till small
    static final long ER_PID = -18;  //error code to declare error with PID
    static final long ER_PIDNotFound = -19;  //error code PID not found
    static final long ER_ISC = -20;  //error code for invalid system call
    static final long IncorrectSizeValue = -21;  //error code for incorrectly passed size

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Other variables
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static long OpValueHolder;//Global variable used to store the opValue and transfer it over to op1Value and op2Value cause they cant be changed with in a function
    static long OpAddressHolder;//Global variable used to store the opAddress and transfer it over to op1Value and op2Value cause they cant be changed with in a function
    static String output = "";//Global String used to print the output to the file
    static int dumpValue = 250; //used to hold the required memory dump size for homework 2
    static long RunningPCBPtr = EndOfList;//used for push and pop
    static long PCBSize = 25;  //used to hold pcbsize, given in class
    static long PCIndex = 20;  //used to access PC in PCB
    static long StackSize = 10;  //set size of stack to 10
    static long StackStartAddrIndex = 5;  //start address of stack in PCB
    static long SPIndex = 19;  //start address of SP in PCB
    static long StackSizeIndex = 6;  //start address of Stack Size in PCB
    static long PriorityIndex = 4;  //start address of priority index in PCB
    static long NextPointerIndex = 0;  //index of next pointer in PCB
    static long PIDIndex = 1;  //index of PID in PCB
    static long ProcessID;  //ID of process
    static long StateIndex = 2;  // index of state in PCB
    static long ReadyState = 1;  //The third location in the PCB holds the state
    static long DefaultPriority = 128;  //default priority assigned to a program
    static long ReasonForWaitingIndex = 3;  //used to set reason for waiting in the running PCB to 'Input Completion Event'.
    static long GPR0Index =  11;  //location of GPR0 in PCB
    static long GPR1Index =  12;  //location of GPR1 in PCB
    static long GPR2Index =  13;  //location of GPR2 in PCB
    static long GPR3Index =  14;  //location of GPR3 in PCB
    static long GPR4Index =  15;  //location of GPR4 in PCB
    static long GPR5Index =  16;  //location of GPR5 in PCB
    static long GPR6Index =  17;  //location of GPR6 in PCB
    static long GPR7Index =  18;  //location of GPR7 in PCB
    static long RQ = -1;  //to be set to PCBptr in InsertIntoRQ
    static long WaitingState = 2;  //used to set the PCB's state to "waiting."
    static long WQ = -1;  //to be set to PCBptr in InsertIntoWQ
    static long PSRIndex = 21;  //index of PSR in PCB
    static long UserMode = 2;  //default user mode in Dispatch
    static long OSMode = 1;  //set to OSMode in dispatch
    static long OSFreeList = EndOfList;	//OS Free memory list is set to empty list. (no 'const' data type because we want to change this value later)
    static long UserFreeList = EndOfList; //User free memory list is set to empty list. (no 'const' data type because we want to change this value later)
    static boolean systemShutdownStatus = false;  //changed to shutdown the system
    static long StartSizeOfUserFreeList = 4000;  //used to set size of user free list
    static long StartSizeOfOSFreeList = 3000;  //used to set size of OS free list
    static long TimeSlice = 200;  //time slice, how many ticks a program gets in the CPU before suspension
    static long TimeSliceExpirationReached = 2;  //used to declare reaching a time slice interrupt
    static long SampleDynamicDumpSize = 249;  //used to declare size of memory dump, given in class



    ///////////////////////////////////////////////////////////////////////////////////////////
    //******************************************************
    //Function: main
    //Author(s): Vito Matthew Eddy
    //Task Description:
    //  initialization the hardware, open the program file and add the data the data to memory, dump memory before cpu run
    //  cpu function executes runs instructions in memory. After cpu  memory is dumped and main ends.
    //Input parameters
    //  None
    //
    //Output parameters
    //  None
    //
    //Function return value
    //  None
    //
    //*****************************************************
    public static void main(String[] args) throws IOException
    {
        long status; //Variable to hold the statuses of CheckandProcessInterrupt and returning CPU values.

        InitializeSystem(); //Call Initialize System function.

        // Run until shutdown.
        while(!systemShutdownStatus)
        {
            //Check and process interrupt.
            status = CheckAndProcessInterrupt(); //Call Check and Process Interrupt function and store return status.
            if(status == ShutdownSystemInterrupt) //If the interrupt is 'shutdown system', exit main.
            {
                break;
            }

            //Dump Ready Queue and Waiting Queue.
            System.out.println("\n\n\nRQ: BEFORE CPU scheduling...");
            output = output + ("\n\n\nRQ: BEFORE CPU scheduling...");
            //Dump the contents of RQ.
            PrintQueue(RQ);
            System.out.println("\nWQ: BEFORE CPU scheduling...");
            output = output + ("\nWQ: BEFORE CPU scheduling...\n");
            //Dump the contents of RQ.//Dump the contents of RQ.
            PrintQueue(WQ);

            dumpMemory("\nDynamic Memory Area BEFORE CPU scheduling:", START_ADDRESS_OF_USER_FREE_LIST, SampleDynamicDumpSize); //Dump the content of the user dynamic memory.

            //Select the next process from RQ to give the CPU to.
            RunningPCBPtr = SelectProcessFromRQ(); //Remove the PCB at the front of the RQ.

            // Perform 'restore context' using Dispatcher function.
            Dispatcher(RunningPCBPtr); //Call Dispatcher function using the Running PCB ptr as an argument.
            System.out.println("\nRQ: AFTER selecting process from RQ...");
            output = output + ("\nRQ: After selecting process from RQ...");
            //Dump the contents of RQ after the front PCB has been removed.
            PrintQueue(RQ);

            System.out.println("\n\nDumping the PCB contents of the RUNNING PCB...");
            output = output + ("\n\nDumping the PCB contents of the RUNNING PCB...");
            //Dump the contents of the running PCB.
            PrintPCB(RunningPCBPtr); //Dump Running PCB and CPU Context passing Running PCB ptr as an argument.

            //Execute instructions of the running process using the CPU.
            System.out.println("\n*CPU HAS BEGUN OPERATING*");
            output = output + ("\n\n*CPU HAS BEGUN OPERATING*");
            status = CPU();  //Call the CPU function.
            System.out.println("*CPU HAS FINISHED OPERATING*");
            output = output + ("\n*CPU HAS FINISHED OPERATING*\n");

            dumpMemory("\nDynamic Memory Area AFTER executing program:", START_ADDRESS_OF_USER_FREE_LIST, SampleDynamicDumpSize); //Dump the content of the user dynamic memory after executing.
            output = output + ("\nDynamic Memory Area AFTER executing program:");

            // Check return status – reason for giving up CPU.
            if(status == TimeSliceExpirationReached) //If the reason is 'time slice expiration', then the process is still active, but ran out of time. Save the context and return it into the RQ.
            {
                System.out.println("\nTIME SLICE HAS EXPIRED, saving context and inserting back into RQ.");
                SaveContext(RunningPCBPtr); //Save CPU Context of running process in its PCB, because the running process is losing control of the CPU.
                InsertIntoRQ(RunningPCBPtr); //Insert running process PCB into RQ.
                RunningPCBPtr = EndOfList; //Set the running PCB ptr to the end of list.
            }

            else if(status == HaltInProgramReached || status < 0) //If the reason is 'halt encountered' or 'error occurred', terminate the process.
            {
                System.out.println("\nHALT REACHED, end of program.");
                output = output + ("\nHALT REACHED, end of program.");
                TerminateProcess(RunningPCBPtr); //Terminate running Process.
                RunningPCBPtr = EndOfList; //Set the running PCB ptr to the end of list.
            }

            else if(status == IO_GETCInterrupt)
            {

                System.out.println("\nINPUT INTERRUPT DETECTED, please enter interrupt for PID: " + memory[(int) (RunningPCBPtr + PIDIndex)]);
                output = output + ("\nINPUT INTERRUPT DETECTED, please enter interrupt for PID: ");
                SaveContext(RunningPCBPtr); //Save CPU Context of running process in its PCB, because the running process is losing control of the CPU.
                memory[(int) (RunningPCBPtr + ReasonForWaitingIndex)] = IO_GETCInterrupt; //Set reason for waiting in the running PCB to 'Input Completion Event'.
                InsertIntoWQ(RunningPCBPtr); //Insert running process into WQ.
                RunningPCBPtr = EndOfList; //Set the running PCB ptr to the end of list.
            }

            else if(status == IO_PUTCInterrupt)
            {
                System.out.println("\nOUTPUT INTERRUPT DETECTED, please enter interrupt for PID: " + memory[(int) (RunningPCBPtr + PIDIndex)]);
                output = output + ("\nOUTPUT INTERRUPT DETECTED, please enter interrupt for PID: ");
                SaveContext(RunningPCBPtr); //Save CPU Context of running process in its PCB, because the running process is losing control of the CPU.
                memory[(int) (RunningPCBPtr + ReasonForWaitingIndex)] = IO_PUTCInterrupt; //Set reason for waiting in the running PCB to 'Output Completion Event'.
                InsertIntoWQ(RunningPCBPtr); //Insert running process into WQ.
                RunningPCBPtr = EndOfList; //Set the running PCB ptr to the end of list.
            }

            else //Unknown programming error encountered.
            {
                System.out.println("\nUnknown programming error encountered. returning error code -19.\n");
                output = output + ("\nUnknown programming error encountered. returning error code -19.\n");
                //return UnknownProgramError;
            }

            System.out.println("------------------------------------------------------------------------------------------------------------------------------");
            output = output + ("\n------------------------------------------------------------------------------------------------------------------------------");

        }

        System.out.println("\nSystem is shutting down. Returning code = 0. Goodbye!\n");
        output = output + ("\nSystem is shutting down. Returning code = 0. Goodbye!\n");

        //return status; //Terminate Operating System.




    } //end of main

    // ************************************************************
    //Function: InitializeSystem
    //Author(s): Vito Matthew Eddy during part 1
    //Author(s): Vito Matthew Eddy during part 2
    //Task Description:
    //  Set all global system hardware components to 0
    //
    //Input Parameters
    //  None
    //
    //Output Parameters
    //  None
    //
    //Function Return Value
    //  None
    // ************************************************************
    public static void InitializeSystem() throws IOException
    {
        gpr = new long[8]; //initialized gprs array
        memory = new long[10000]; //initialized main memory array



        //initialize all 10,000 memory address to zero
        for (int i = 0; i < MAX_MEMORY_ADDRESS; i++)
        {
            memory[i] = 0; //set all values to zero
        }

        //initialize all 8 gpr values to 0 using for loop
        for (int i = 0; i < 8; i++)
        {
            gpr[i] = 0; //set all gprs to zero
        }

        //initial blocks
        UserFreeList = START_ADDRESS_OF_USER_FREE_LIST;
        memory[(int)START_ADDRESS_OF_USER_FREE_LIST] = EndOfList;
        memory[(int)START_ADDRESS_OF_USER_FREE_LIST + 1] = StartSizeOfUserFreeList;
        OSFreeList = START_ADDRESS_OF_OS_FREE_LIST;
        memory[(int)START_ADDRESS_OF_OS_FREE_LIST] = EndOfList;
        memory[(int)START_ADDRESS_OF_OS_FREE_LIST + 1] = StartSizeOfOSFreeList;



        MAR = MBR = CLOCK = IR = PSR = PC = SP = 0; //Other hardware components initialized to 0

        String NullProcess = "src/Null.txt";//Null process src is the souce folder we used  

        CreateProcess(NullProcess,0);//Create process for null process 

    }//end of InitializeSystem system


    // ********************************************************************
    // Function: AbsoluteLoader
    // Author(s): Vito Matthew Eddy during part 1
    // Task Description:
    // Open the file containing HYPO machine user program and
    // load the content into HYPO memory.
    // On successful load, return the PC value in the End of Program line.
    // On failure, display appropriate error message and return appropriate error code
    //
    // Input Parameters
    // filename Name of the Hypo Machine executable file
    //
    // Output Parameters
    //  None
    //
    // Function Return Value will be one of the following:
    // OPENING_FILE_ERROR -5 Unable to open the file
    // INVALID_MEMORY_RANGE_ERROR -3 Invalid address error
    // NO_END_OF_PROGRAM_INDICATOR_ERROR -4 Missing end of program indicator
    // INVALID_PC_VALUE_ERROR -2 invalid PC value
    // 0 to Valid address range Successful Load, valid PC value
    // ************************************************************
    public static int AbsoluteLoader(String filename) throws FileNotFoundException
    { //Address of filename inputted.
        File fileObj = new File(filename); //create new file object and open the file given from the user

        try//try used inorder to open buffer and catch input out error
        {
            String fileLine; //use to hold the one line of the file
            BufferedReader bufferReaderObj = new BufferedReader(new FileReader(fileObj)); //create buffer reader object to hold each line of the file


            while ((fileLine = bufferReaderObj.readLine()) != null)//loop will run till there are no more lines in the file
            {
                String[] argument = fileLine.split("    ");//split method splits the string up by tab which is 4 spaces

                long memAddress = Long.parseLong(argument[0]);//the memory address is the first argument

                long instruction = Long.parseLong(argument[1]);//the instruction the second argument


                if (memAddress >= 0 && memAddress <= ADDRESS_RANGE_CAP) //checks if the memory address is in a valid rang
                {
                    memory[(int)memAddress] = instruction; //saves instruction to corresponding memory address
                }

                else if (memAddress == EndOfMachLangProgram) //once the -1 address is reach program file is no longer loaded
                {
                    bufferReaderObj.close();//close buffer

                    if (instruction >= 0 && instruction <= ADDRESS_RANGE_CAP) //checks if PC value is within range and return PC value.
                    {
                        System.out.println("Program successfully loaded.");
                        output = output + ("\nProgram successfully loaded.\n");

                        return (int)instruction; // This is the PC value or the start of the program.
                    }

                    else //PC value is incorrect.
                    {
                        System.out.println("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                        output = output + ("\nINVALID_PC_VALUE_ERROR returning error code -2.");

                        return INVALID_PC_VALUE_ERROR;// Return INVALID_PC_VALUE_ERROR code
                    }
                }

                else if (memAddress > ADDRESS_RANGE_CAP || memAddress < -1) //Memory is in an invalid range.
                {
                    System.out.println("\nINVALID_MEMORY_RANGE_ERROR returning error code -3.");
                    output = output + ("\nINVALID_MEMORY_RANGE_ERROR returning error code -3.");

                    return INVALID_MEMORY_RANGE_ERROR; // Return INVALID_MEMORY_RANGE_ERROR code
                }

            }//end of while loop
            System.out.println("\nNO_END_OF_PROGRAM_INDICATOR_ERROR returning error code -4.");
            output = output + ("\nNO_END_OF_PROGRAM_INDICATOR_ERROR returning error code -4.");

            return NO_END_OF_PROGRAM_INDICATOR_ERROR; // Return NO_END_OF_PROGRAM_INDICATOR_ERROR code
        }
        catch (IOException e) //catch IOException or file not found exception
        {

            System.out.println("\nOPENING_FILE_ERROR returning error code -5.");
            output = output + ("\nOPENING_FILE_ERROR returning error code -5.");

            return OPENING_FILE_ERROR; // Return OPENING_FILE_ERROR code
        }

    } //end of AbsoluteLoader

    // ************************************************************
    // Function: DumpMemory
    // Author(s): Vito Matthew Eddy during part 1
    // Task Description:
    // Displays a string passed as one of the input parameter.
    // Displays content of GPRs, SP, PC, PSR, system Clock and
    // the content of specified memory locations in a specific format.
    //
    // Input Parameters
    // String to be displayed
    // StartAddress Start address of memory location
    // Size Number of locations to dump
    // Output Parameters
    // None
    //
    // Function Return Value
    // None
    // ************************************************************
    public static void dumpMemory(String string, long StartAddress, long size) throws IOException
    {


        PrintWriter printer = new PrintWriter("output.txt");//create print writer object to write to file
        if(StartAddress < 0 || StartAddress > MAX_MEMORY_ADDRESS || size < 1 || StartAddress+size > MAX_MEMORY_ADDRESS)//checks for invalid address size
        {
            System.out.println("There is either an invalid start address, end address, or size. Cannot continue");//Display if there is an error
            output = output + ("There is either an invalid start address, end address, or size. Cannot continue");

        }
        else
        {
            //each string is spaced out by a tab G0, G1, G2 etc each take up two spaces and then it is tab
            System.out.printf("GPR's:  %-4s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s", " ","G0","G1","G2","G3","G4","G5","G6","G7","SP","PC" );
            //each gpr takes up a max of 7 spaces each is then space out by a tab
            System.out.printf("\n        %-4s%-7d%-7d%-7d%-7d%-7d%-7d%-7d%-7d%-7d%-7d", " ",gpr[0], gpr[1], gpr[2], gpr[3], gpr[4], gpr[5], gpr[6], gpr[7], SP, PC);
            //each string is spaced out by a tab +0 +1 +2 etc each take up two spaces and then it is tab
            System.out.printf("\n\nAddress:%-4s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s", " ","+0","+1","+2","+3","+4","+5","+6","+7","+8","+9" );

            //Write to file
            //each of these formatted strings are append  into the global output string which is used later to write to the output file
            output = output + String.format("GPR's:  %-4s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s", " ","G0","G1","G2","G3","G4","G5","G6","G7","SP","PC" );
            output = output + String.format("\n        %-4s%-7d%-7d%-7d%-7d%-7d%-7d%-7d%-7d%-7d%-7d", " ",gpr[0], gpr[1], gpr[2], gpr[3], gpr[4], gpr[5], gpr[6], gpr[7], SP, PC);
            output = output + String.format("\n\nAddress:%-4s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s", " ","+0","+1","+2","+3","+4","+5","+6","+7","+8","+9" );



            int endAddress = (int)StartAddress + dumpValue;//set the end address
            while((int)StartAddress < endAddress)//looping until the end address is reached
            {

                System.out.printf("\n%-12d", StartAddress); //printing the start address
                output = output+String.format("\n%-12d", StartAddress); //appending the start address to the output string
                for (int i = 0 ; i<10 ; i++)// Display 10 values of memory from addr to addr+9
                {
                    if(StartAddress <= endAddress)
                    {
                        System.out.printf("%-7d",memory[(int)StartAddress]);
                        output = output + String.format("%-7d",memory[(int)StartAddress]);// display and increment address
                        StartAddress++;
                    }//end of if
                    else
                    {
                        break;
                    }//end of else
                }//end of for loop


            }//end of while loop
            //displays clock and psr
            System.out.printf("\nClock:%d\n", CLOCK);
            output = output + String.format("\nClock:%d\n", CLOCK);//save it to output string

            System.out.printf("PSR:%d\n", PSR);
            output =  output + String.format("PSR:%d\n", PSR);//save it to output string

            //write memory dump to a new file
            printer.print(output);



        }//end of else
        printer.close();//close printer
    }//end of dumpMemory

    //************************************************************
    // Function: FetchOperand
    // Author(s): Vito Matthew Eddy during part 1
    // Task Description:
    //  Function takes in the operand mode, operand register, operand address, operand value
    //
    //  operand mode can be one of six modes
    //  1 is Register Mode
    //  2 is Register deferred mode
    //  3 is Autoincrement mode
    //  4 is Auto decrement mode
    //  5 is Direct mode
    //  6 is Immediate mode
    //
    //  opAddress and OpValue change depending on the mode
    //  OpValueHolder saves the value of the OpValue because in java a variable can not be changed within a function unless it is returned
    //
    // Input Parameters
    // OpMode Operand mode value
    // OpReg Operand GPR value
    //
    // Output Parameters
    // OpAddress Address of operand
    // OpValue Operand value when mode and GPR are valid
    //
    // Function Return Value
    // OK On successful fetch
    // INVALID_ADDRESS_IN_GPR_ERROR -6
    // INVALID_PC_VALUE_ERROR -2
    // INVALID_MODE_ERROR -7
    // List all possible error codes here
    //************************************************************
    public static long fetchOperand(long OpMode, long OpReg, long OpAddress, long OpValue)
    {
        // Fetch operand value based on the operand mode
        switch ((int)OpMode)
        {
            case 1:// Register Mode
                OpAddress=-2;// set to any negative value
                OpValue=gpr[(int)OpReg];// operand value is in the register
                OpValueHolder = OpValue;//used to save OpValue and transfer it to Op1Value or Op2value
                OpAddressHolder = OpAddress;//used to save OpAddress and transfer it to Op1Value or Op2value
                break;

            case 2: // Register deferred mode

                OpAddress = gpr[(int)OpReg]; // Op address is in the register

                if(OpAddress >= START_ADDRESS_OF_USER_FREE_LIST && OpAddress <= END_ADDRESS_OF_USER_FREE_LIST)//checks if OpAddress is in valid range
                {
                    OpValue = memory[(int)OpAddress];// operand value is in the register
                    OpValueHolder = OpValue;//used to save OpValue and transfer it to Op1Value or Op2value
                    OpAddressHolder = OpAddress;//used to save OpAddress and transfer it to Op1Value or Op2value
                }
                else
                {
                    System.out.println(OpAddress);
                    System.out.println("\nINVALID_ADDRESS_IN_GPR_ERROR returning error code -6. Register deferred mode");//Display invalid address error message and Return invalid address error code
                    return INVALID_ADDRESS_IN_GPR_ERROR;
                }
                break;
            case 3:// Autoincrement mode

                OpAddress = gpr[(int)OpReg];// operand value is in the register
                if(OpAddress >= START_ADDRESS_OF_USER_FREE_LIST && OpAddress <= END_ADDRESS_OF_USER_FREE_LIST)//checks if OpAddress is in valid range
                {
                    OpValue = memory[(int)OpAddress];// operand in memory
                    OpValueHolder = OpValue;//used to save OpValue and transfer it to Op1Value or Op2value
                    OpAddressHolder = OpAddress;//used to save OpAddress and transfer it to Op1Value or Op2value
                }
                else
                {
                    System.out.println("This is the OpAddress :"  + OpAddress);
                    System.out.println("\nINVALID_ADDRESS_IN_GPR_ERROR returning error code -6. Autoincrement mode");//Display invalid address error message and Return invalid address error code
                    return INVALID_ADDRESS_IN_GPR_ERROR;
                }
                gpr[(int)OpReg]++;// Increment register content/value by 1
                break;
            case 4:  // Auto decrement mode

                --gpr[(int)OpReg];// Decrement register value (content) by 1
                OpAddress = gpr[(int)OpReg];// Op address is in the register
                if(OpAddress >= START_ADDRESS_OF_USER_FREE_LIST && OpAddress <= END_ADDRESS_OF_USER_FREE_LIST)//checks if OpAddress is in valid range
                {
                    OpValue = memory[(int)OpAddress]; // operand in memory
                    OpValueHolder = OpValue;//used to save OpValue and transfer it to Op1Value or Op2value
                    OpAddressHolder = OpAddress;//used to save OpAddress and transfer it to Op1Value or Op2value
                }
                else
                {
                    System.out.println("\nINVALID_ADDRESS_IN_GPR_ERROR returning error code -6. Auto decrement mode");//Display invalid address error message and Return invalid address error code
                    return INVALID_ADDRESS_IN_GPR_ERROR;
                }
                break;
            case 5:  // Direct mode



                OpAddress = memory[(int)PC++];//check for valid address in PC and Increment PC after fetching

                if(OpAddress >= START_ADDRESS_OF_USER_PROGRAM_AREA && OpAddress <= START_ADDRESS_OF_USER_FREE_LIST)//checks if OpAddress is in valid range
                {
                    OpValue = memory[(int)OpAddress];// operand in memory
                    OpValueHolder = OpValue;//used to save OpValue and transfer it to Op1Value or Op2value
                    OpAddressHolder = OpAddress;//used to save OpAddress and transfer it to Op1Value or Op2value

                }
                else //if not display msg & return error
                {
                    System.out.println("\nINVALID_PC_VALUE_ERROR returning error code -2.");//display invalid pc error and returns error code
                    return INVALID_PC_VALUE_ERROR;
                }

                break;
            case 6://   Immediate mode

                if (PC <= ADDRESS_RANGE_CAP && PC >= START_ADDRESS_OF_USER_PROGRAM_AREA)//checks if PC is in valid range
                {
                    OpAddress=-99;// set to any negative number
                    OpValue = memory[(int)PC++];// Increment PC after fetching value

                    OpValueHolder = OpValue;//used to save OpValue and transfer it to Op1Value or Op2value
                    OpAddressHolder = OpAddress;//used to save OpAddress and transfer it to Op1Value or Op2value
                }
                else
                {
                    System.out.println("\nINVALID_PC_VALUE_ERROR returning error code -2.");//display invalid pc error and returns error code
                    return INVALID_PC_VALUE_ERROR;
                }
                break;
            default:// Invalid mode
                System.out.println("\nINVALID_MODE_ERROR returning error code -7");//Displays invalid mode error and returns error code
                return INVALID_MODE_ERROR;
        }
        return OK;// return success status
    }//end of fetch

    // ************************************************************
    // Function: CPU
    // Author(s): Vito Matthew Eddy during part 1
    // Author(s): Vito Matthew Eddy during part 2
    // Task Description:
    // CPU loads the data from the program file and starts executing instructions. The cpu starts executing instructions at the pc value which is the value that tab from the -1 in the program file
    //  The pc value is then set to the mar value which accesses that address in memory that instruction is then saved to the MBR which is then set to the IR which is then decoded
    //  The IR is broken down into five different parts
    //
    //  opCode or Instruction is when we want to Halt (0), Add (1), Subtract (2), Multiply (3), Divide (4), Move (5), Branch (6), Branch on minus (7), Branch on Plus (8), Branch on zero (9), Push (10), Pop (11), System Call (12)
    //  op1mode or Addressing mode  Register mode (1), Register deferred (2), Autoincrement (3), Auto decrement (4), Direct mode (5), Immediate mode (6)
    //  op1gpr the gpr you want to use between 0 and 7 ,however in modes (5) and (6) the gpr is not used
    //  op2mode same as above Addressing mode  Register mode (1), Register deferred (2) etc
    //  op2gpr the second gpr you want to use between 0 and 7 ,however in modes (5) and (6) the gpr is not used
    //
    // Input Parameters
    // None
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // HaltInProgramReached
    // INVALID_PC_VALUE_ERROR -2
    // INVALID_MODE_ERROR -7
    // INVALID_OPERAND_GPR_ERROR - 8
    // DIVIDE_BY_ZERO_ERROR -9
    // STACK_OVERFLOW_ERROR -10
    // STACK_UNDERFLOW_ERROR -11
    // INVALID_OP_CODE_ERROR -12
    // UNKNOWN_PROGRAM_ERROR -13
    // ************************************************************
    public static long CPU()
    {
        // Declare local variables as needed
        long opCode, remainder, op1mode, op1gpr, op2mode, op2gpr, status, op1Address, op1Value, op2Address, op2Value, result;
        long TimeLeft = TimeSlice; //Set Timeleft for process to 200 ticks.
        status = op1Address = op1Value = op2Address = op2Value = 0;


        boolean haltReached = false;//not halt is found yet so set this to false so our wile loop can run

        while(!haltReached && TimeLeft > 0)//wile will execute until a halt or an error occurs and breaks the while loop
        {

            if (PC >= 0 && PC <= ADDRESS_RANGE_CAP)//checks if pc is in program area
            {
                MAR = PC++; // Set MAR to PC value and advance PC by 1 to point to next word.
                MBR = memory[(int)MAR];
            }
            else
            { //PC is not in valid range.
                System.out.println("\nINVALID_PC_VALUE_ERROR returning error code -2.");//display invalid pc error and returns error code
                return INVALID_PC_VALUE_ERROR; //Returns invalid PC address error.
            }

            IR=MBR;//Copy MBR value into instruction register IR;
            //Decoding cycle

            opCode = IR / 10000; // Integer division, gives quotient gets the
            remainder = IR % 10000;// Modulo (%) gives remainder of integer division

            op1mode = remainder / 1000; // Integer division, gives quotient gets the
            remainder = remainder % 1000;// Modulo (%) gives remainder of integer division

            op1gpr = remainder / 100; // Integer division, gives quotient gets the
            remainder = remainder % 100;// Modulo (%) gives remainder of integer division

            op2mode = remainder / 10;// Integer division, gives quotient gets the
            remainder = remainder % 10;// Modulo (%) gives remainder of integer division

            op2gpr = remainder; //remainder is the last number which is op2gpr

            if(op1mode < 0 || op1mode > 6 || op2mode < 0 || op2mode > 6) //check if its a valid mode
            {
                System.out.println("\nINVALID_MODE_ERROR returning error code -7");//Displays error
                return INVALID_MODE_ERROR; //Returns invalid operand GPR error.
            }
            if(op1gpr < 0 || op1gpr > 7 || op2gpr < 0 || op2gpr > 7) //check if its a valid gpr
            {
                System.out.println("\nINVALID_OPERAND_GPR_ERROR returning error code -8.");//Displays error
                return INVALID_OPERAND_GPR_ERROR; //Returns invalid operand GPR error.
            }


            switch((int)opCode)
            {
                case 0: //Halt 0
                    haltReached = true;//end the wile loop
                    System.out.println("CPU Halt encountered");//Display the instruction happen
                    output = output + ("\nCPU Halt encountered");
                    CLOCK = CLOCK+12;
                    TimeLeft = TimeLeft - 12; //Update time left.

                    break;
                case 1: //Add 1
                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the op1Value
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    status = fetchOperand(op2mode, op2gpr, op2Address, op2Value);
                    op2Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the op2Value
                    op2Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    result = op1Value + op2Value; //Add the fetched values
                    if (op1mode == 1)//checks if op1mode is in immediate mode
                    { // Op1Mode is in register mode.
                        gpr[(int) op1gpr] = result; //Store result in gpr array at op1gpr;
                    }
                    else if (op1mode == 6) // Op1Mode is in immediate mode. display error.
                    {
                        System.out.println("\nINVALID_MODE_ERROR returning error code -7");//display Destination operand mode cannot be immediate mode error
                        output = output + ("\nINVALID_MODE_ERROR returning error code -7");

                        return INVALID_MODE_ERROR; //Returns invalid mode error.
                    }

                    else
                    {
                        memory[(int) op1Address] = result;//Store Result in hypo memory at location Op1Address
                    }
                    CLOCK = CLOCK + 3;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 3; //Update time left.

                    break;

                case 2:  //Subtract 2

                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    status = fetchOperand(op2mode, op2gpr, op2Address, op2Value);
                    op2Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the op2Value
                    op2Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    result = op1Value - op2Value; //subtract the fetched values.
                    if (op1mode == 1)
                    { // Op1Mode is in register mode.
                        gpr[(int) op1gpr] = result; //Store result in GPR array at op1gpr;
                    }
                    else if (op1mode == 6) // Op1Mode is in immediate mode. Error.
                    {
                        System.out.println("\nINVALID_MODE_ERROR returning error code -7");
                        output = output + ("\nINVALID_MODE_ERROR returning error code -7");
                        return INVALID_MODE_ERROR; //Returns invalid mode error.
                    }

                    else
                    {
                        memory[(int) op1Address] = result;//Store Result in hypo memory at location Op1Address
                    }
                    CLOCK = CLOCK + 3;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 3; //Update time left.

                    break;
                case 3: //Multiply 3

                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    status = fetchOperand(op2mode, op2gpr, op2Address, op2Value);
                    op2Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op2Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    result = op1Value * op2Value; //multiply the fetched values.
                    if (op1mode == 1)
                    { // Op1Mode is in register mode.
                        gpr[(int) op1gpr] = result; //Store result in GPR array at op1gpr;
                    }
                    else if (op1mode == 6) // Op1Mode is in immediate mode. Error.
                    {
                        System.out.println("\nINVALID_MODE_ERROR Op1mode is in immediate mode returning error code -7");
                        output = output + ("\nINVALID_MODE_ERROR Op1mode is in immediate mode returning error code -7");

                        return INVALID_MODE_ERROR; //Returns invalid mode error.
                    }

                    else
                    {
                        memory[(int) op1Address] = result;//Store Result in hypo memory at location Op1Address
                    }
                    CLOCK = CLOCK + 6;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 6; //Update time left.

                    break;
                case 4: //Divide 4

                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    status = fetchOperand(op2mode, op2gpr, op2Address, op2Value);
                    op2Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op2Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    if(op2Value == 0)//checks if the user is trying to divide by zero
                    {
                        System.out.println("\nDIVIDE_BY_ZERO_ERROR returning error code -8.");
                        output = output + ("\nDIVIDE_BY_ZERO_ERROR returning error code -8.");
                        return DIVIDE_BY_ZERO_ERROR; // Returns divide by zero error.
                    }
                    result = op1Value / op2Value; //divide the fetched values.
                    if (op1mode == 1)
                    { // Op1Mode is in register mode.
                        gpr[(int) op1gpr] = result; //Store result in GPR array at op1gpr;
                    }
                    else if (op1mode == 6) // Op1Mode is in immediate mode. Error.
                    {
                        System.out.println("\nINVALID_MODE_ERROR Op1mode is in immediate mode returning error code -7");
                        output = output + ("\nINVALID_MODE_ERROR Op1mode is in immediate mode returning error code -7");
                        return INVALID_MODE_ERROR; //Returns invalid mode error.
                    }

                    else
                    {
                        memory[(int) op1Address] = result;//Store Result in hypo memory at location Op1Address
                    }
                    CLOCK = CLOCK + 6;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft + 6; //Update time left.

                    break;
                case 5:  //Move

                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    status = fetchOperand(op2mode, op2gpr, op2Address, op2Value);
                    op2Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op2Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status; // Returns error code found while fetching.
                    }
                    result = op2Value; //set the fetched values to result.
                    if (op1mode == 1)// Op1Mode is in register mode.
                    {
                        gpr[(int) op1gpr] = result; //Store result in GPR array at op1gpr
                    }
                    else if (op1mode == 6) // Op1Mode is in immediate mode. Error.
                    {
                        System.out.println("\nINVALID_MODE_ERROR Op1mode is in immediate mode returning error code -7");
                        output = output + ("\nINVALID_MODE_ERROR Op1mode is in immediate mode returning error code -7");
                        return INVALID_MODE_ERROR; //Returns invalid mode error.
                    }

                    else
                    {

                        memory[(int) op1Address] = result;
                    }
                    CLOCK = CLOCK + 2;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 2; //Update time left.

                    break;
                case 6:  //Branch

                    if (PC >= 0 && PC <= ADDRESS_RANGE_CAP)//checks if pc is in valid range
                    {
                        PC = memory[(int) PC]; //Branches to the memory location.
                    }
                    else
                    {
                        System.out.println("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                        output = output + ("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                        return INVALID_PC_VALUE_ERROR; //Returns invalid PC address error.
                    }
                    CLOCK = CLOCK + 2;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 2; //Update time left.

                    break;
                case 7: //Branch on minus

                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    if(status < 0)
                    {
                        return status;
                    }
                    if (op1Value < 0) // If Op1 < 0, branch to desired PC location.
                    {
                        if (PC >= 0 && PC <= ADDRESS_RANGE_CAP)//checks if pc is in valid range
                        {
                            PC = memory[(int) PC];//Set PC to memory[PC]
                        }
                        else
                        { //PC is not in valid range
                            System.out.println("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                            output = output + ("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                            return INVALID_PC_VALUE_ERROR; //Returns invalid PC address error.
                        }
                    }
                    else
                    {
                        PC++;//Increment pc value by 1
                    }
                    CLOCK = CLOCK + 4;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 4; //Update time left.

                    break;
                case 8: //Branch on plus 8

                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    if(status < 0)//check for error
                    {
                        return status;
                    }

                    if (op1Value > 0) // If Op1 < 0, branch to desired PC location.
                    {
                        if (PC >= 0 && PC <= ADDRESS_RANGE_CAP)//check if pc is in valid range
                        {
                            PC = memory[(int) PC];//Set PC to memory[PC]
                        }
                        else
                        { //PC is not in valid range
                            System.out.println("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                            output = output + ("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                            return INVALID_PC_VALUE_ERROR; //Returns invalid PC address error.
                        }
                    }
                    else
                    {
                        PC++;//Increment pc value by 1
                    }
                    CLOCK = CLOCK + 4;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 4; //Update time left.

                    break;
                case 9: //Branch on zero 9

                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    if(status < 0)//check for error
                    {
                        return status;
                    }
                    if (op1Value == 0) // If Op1 < 0, branch to desired PC location.
                    {
                        if (PC >= 0 && PC <= ADDRESS_RANGE_CAP)
                        {
                            PC = memory[(int) PC];//Set PC to memory[PC]
                        }
                        else
                        { //PC is not in valid range
                            System.out.println("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                            output = output + ("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                            return INVALID_PC_VALUE_ERROR; //Returns invalid PC address error.
                        }
                    }
                    else
                    {
                        PC++;//Increment pc value by 1
                    }
                    CLOCK = CLOCK + 4;//Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 4; //Update time left.

                    break;
                case 10: //Push 10

                    //Vito worked on this section
                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    if(status < 0)//check for error
                    {
                        return status;
                    }
                    if (SP == memory[(int)RunningPCBPtr + (int) STACK_START_ADDRESS_INDEX] + STACK_SIZE)//check for overflowed error and dispaly
                    {
                        System.out.println("\nSTACK_OVERFLOW_ERROR returning error code -10.");
                        output = output + ("\nSTACK_OVERFLOW_ERROR returning error code -10.");

                        return STACK_OVERFLOW_ERROR;
                    }
                    else
                    {
                        SP++;//Increment sp value by 1
                        memory[(int)SP] = op1Value;
                    }

                    System.out.println("Pushing the value: " + memory[(int)SP] + " to the RUNNING PCB's stack.");//display value pushing
                    output = output + ("\nPushing the value: " + memory[(int)SP] + " to the RUNNING PCB's stack.");
                    CLOCK = CLOCK + 2; //Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 2; //Update time left.
                    break;
                case 11: //Pop 11


                    //Eddy worked on this section
                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status;
                    }

                    if (SP < memory[(int)RunningPCBPtr + (int) STACK_START_ADDRESS_INDEX])//checks for under flow error
                    {
                        System.out.println ("\nSTACK_UNDERFLOW_ERROR returning error code -11.");//display error
                        output = output + ("\nSTACK_UNDERFLOW_ERROR returning error code -11.");
                        return STACK_UNDERFLOW_ERROR;
                    }

                    else
                    {
                        System.out.println("Popping the value: " + memory[(int)SP] + " from the RUNNING PCB's stack.");
                        output = output + ("\nPushing the value: " + memory[(int)SP] + " from the RUNNING PCB's stack.");
                        op1Address = memory[(int)SP]; //Store (pop) top value on stack at Op1Address.
                        SP--; // Decrement SP by 1
                    }

                    CLOCK = CLOCK + 2; //Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 2; //Update time left.
                    break;
                case 12: //System call 12

                    //Matthew worked on this section

                    if(PC < START_ADDRESS_OF_USER_PROGRAM_AREA || PC > ADDRESS_RANGE_CAP) //Check to see if PC is in User Program Area.
                    {
                        System.out.println ("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                        output = output + ("\nINVALID_PC_VALUE_ERROR returning error code -2.");
                        return INVALID_PC_VALUE_ERROR; //Returns invalid PC address error.
                    }
                    status = fetchOperand(op1mode, op1gpr, op1Address, op1Value);
                    op1Value = OpValueHolder;//saves the OpValue from the fetchOperand function into the opValue
                    op1Address = OpAddressHolder;//saves the OpAdress from fetch
                    //check for error
                    if (status < 0)
                    {
                        return status;
                    }
                    status = SystemCall(op1Value);
                    if(status == IO_GETCInterrupt || status == IO_PUTCInterrupt)
                        return status; //If IO_GETC or IO_PUTC is encountered, an interrupt has occurred. Return from CPU to process accordingly.

                    CLOCK = CLOCK + 12; //Increment clock by instruction execution time
                    TimeLeft = TimeLeft - 12; //Update time left.
                    break;
                default:
                    System.out.println ("\nINVALID_OP_CODE_ERROR returning error code -12.");//display error
                    output = output + ("\nINVALID_OP_CODE_ERROR returning error code -12.");
                    return INVALID_OP_CODE_ERROR; //Returns invalid opCode error.

            }//end of switch

        }//end of while loop
        if(haltReached)
        {
            return HaltInProgramReached; //If the reason why the CPU was given up was because of a halt in the program, then return that to main.
        }

        else if(TimeLeft <= 0)
        {
            return TimeSliceExpirationReached; //If the reason why the CPU was given up was because of a time slice expiration in the program, then return that to main.
        }

        else
        {
            return UNKNOWN_PROGRAM_ERROR; //Any other reason why CPU is left is due to unknown programming errors.
        }
    }//end of CPU



    // ************************************************************
    // Function: Create Process
    // Author(s): Matthew
    // Task Description:
    // This call dynamically creates at run time a  process with a specific priority and id number. Up to 256 priority levels
    // can be specified. Any unique number other than 0 that is not already in use can be a PID. 0 represents no PID is assigned
    // The PCB of the newly created process is places in the RQ immediatley in front of the PCB's of all other programs with
    // the same priority, this call forces rescheduling.
    // Input Parameters
    // FileName of file containing code to create process
    // priority to assign a priority to the process
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // ERROR: not enough memory available. Error code = ErrorNoMem
    // ERROR: The requested memory size is too small. Error code ErrornoMem
    // ERROR: OPENING_FILE_ERROR returning error code -5.
    // ERROR: NO_END_OF_PROGRAM_INDICATOR_ERROR -4 Missing end of program indicator
    // ERROR: Invalid memory range found. Returning error code -3
    // ERROR: Invalid PC value. Error code = InvalidPCValue
    // Program Successfully loaded return code: 0k
    // ************************************************************
    //Create process
    public static long CreateProcess(String fileName, long priority) throws FileNotFoundException, IOException
    {
        //Allocate space for Process Control Block
        long PCBptr = AllocateOSMemory(PCBSize); // return value contains address or erro
        if(PCBptr < 0){ //Check for error and return error code, if memory allocation failed
            return PCBptr;
        }

        InitializePCB(PCBptr);// Initialize PCB: Set nextPCBlink to end of list, default priority, Ready state, and PID
        
        //Load that program
        long value = AbsoluteLoader(fileName);
        
        if(value < 0){
            return value;//Return error code pertaining to Absolute Loader error.
        }

        memory[(int)PCBptr + (int)PCIndex] = value;

        // Allocate stack space from user free list
        long ptr = AllocateUserMemory(StackSize);
        if(ptr < 0){
            FreeOSMemory(PCBptr, PCBSize);
            return ptr;
        }

        // Store stack information in the PCB – SP, ptr, and size
        memory[(int)PCBptr + (int)StackStartAddrIndex] = ptr;//Set Starting Stack Address in the PCB.
        memory[(int)PCBptr + (int)SPIndex] = ptr - 1; //Empty stack is low address, full stack is high address. Subtract 1 because empty address is one prior to start address.
        memory[(int)PCBptr + (int)StackSizeIndex] = StackSize;//Set Stack Size in the PCB.
        memory[(int)PCBptr + (int)PriorityIndex] = priority;//Set Priority (passed as argument) in the PCB.

        dumpMemory("\nDumping memory addresses in User Program Area pertaining to the four machine language programs written.", START_ADDRESS_OF_USER_PROGRAM_AREA, ADDRESS_RANGE_CAP); //Dump program area.


        PrintPCB(PCBptr); // Print the contents of the PCB.
        InsertIntoRQ(PCBptr); // Insert PCB into Ready Queue according to the scheduling algorithm.

        return OK;
    }//end of create process


    // ************************************************************
    // Function: Initialize PCB
    // Author(s): Eddy
    // Task Description:
    // This function call is passed PCBptr, that points to the memory address of the PCB to initialize,
    // and sets all of it's contents to the appropriate values.
    // the PCB positions are determined by the pseudocode provided in class
    // Input Parameters
    // PCBptr, The memory location of the PCB.
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    //Initialize PCB
    public static void InitializePCB(long PCBptr)
    {
        // Array initialization Set entire PCB area to 0 using PCBptr
        for (int PCBindex = 0; PCBindex < PCBSize; PCBindex++)
                memory[(int)PCBptr + PCBindex] = 0;

        memory[(int)PCBptr + (int)NextPointerIndex] = EndOfList; // EndOfList is a constant set to -1
        memory[(int)PCBptr + (int)PIDIndex] = ProcessID++;  // ProcessID is global variable initialized to 1
        memory[(int)PCBptr + (int)StateIndex] = ReadyState; //The third location in the PCB holds the state. Set the state to ReadyState (1).
        memory[(int)PCBptr + (int)PriorityIndex] = DefaultPriority; //The fifth location in the PCB holds the priority. Set the priority to DefaultPriority (128).



    }//end of Initialize PCB
    // ************************************************************
    // Function: Print PCB
    // Author(s): Eddy
    // Task Description:
    // This function call is passed PCBptr, that points to the memory address of the PCB to print,
    // it's PID, state, priority, GPRs, SP, and PC.
    // the PCB positions are determined by the pseudocode provided in class
    //
    // Input Parameters
    // PCBptr, The memory location of the PCB.
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    public static void PrintPCB(long PCBptr){
        //Prints PCB address, Next Pointer Address, PID, State, Priority, PC, and SP values of the PCB.
        System.out.println("\nContents of the PCB in memory address " + PCBptr + ": ");
        output = output + ("\nContents of the PCB in memory address " + PCBptr + ": ");
        // Format for Printing PCB.
        //    PCB Addr = 7500, NextPCBptr = -1,    PID = 1,  State = 1,  Reason code = 1
        //    Stack Addr = 2500, Stack size = 10,  SP = 2501, PC = 100
        System.out.println("PCB address = " + PCBptr + ", Next PCB Ptr = " + memory[(int)PCBptr + (int)NextPointerIndex] + ", PID = " + memory[(int)PCBptr + (int)PIDIndex] + ", State = " + memory[(int)PCBptr + (int)StateIndex] + ", Reason for Waiting = " + memory[(int)PCBptr + (int)ReasonForWaitingIndex] + ", PC = " + memory[(int)PCBptr + (int)PCIndex] + ", SP = " + memory[(int)PCBptr + (int)SPIndex] + ", Priority = " + memory[(int)PCBptr + (int)PriorityIndex] + ", STACK INFO: Starting Stack Address " + memory[(int)PCBptr + (int)StackStartAddrIndex] + ", Stack Size = " + memory[(int)PCBptr + (int)StackSizeIndex]);
        output = output + ("PCB address = " + PCBptr + ", Next PCB Ptr = " + memory[(int)PCBptr + (int)NextPointerIndex] + ", PID = " + memory[(int)PCBptr + (int)PIDIndex] + ", State = " + memory[(int)PCBptr + (int)StateIndex] + ", Reason for Waiting = " + memory[(int)PCBptr + (int)ReasonForWaitingIndex] + ", PC = " + memory[(int)PCBptr + (int)PCIndex] + ", SP = " + memory[(int)PCBptr + (int)SPIndex] + ", Priority = " + memory[(int)PCBptr + (int)PriorityIndex] + ", STACK INFO: Starting Stack Address " + memory[(int)PCBptr + (int)StackStartAddrIndex] + ", Stack Size = " + memory[(int)PCBptr + (int)StackSizeIndex]);
        //Prints the GPR values of the PCB.
        System.out.println("GPRs:   GPR0: " + memory[(int)PCBptr + (int)GPR0Index] + "   GPR1: " + memory[(int)PCBptr + (int)GPR1Index] + "   GPR2: " + memory[(int)PCBptr + (int)GPR2Index] + "   GPR3: " + memory[(int)PCBptr + (int)GPR3Index]  + "   GPR4: " + memory[(int)PCBptr + (int)GPR4Index] + "   GPR5: " + memory[(int)PCBptr + (int)GPR5Index] + "   GPR6: " + memory[(int)PCBptr + (int)GPR6Index] + "   GPR7: " + memory[(int)PCBptr + (int)GPR7Index] + "\n");
        output = output + ("GPRs:   GPR0: " + memory[(int)PCBptr + (int)GPR0Index] + "   GPR1: " + memory[(int)PCBptr + (int)GPR1Index] + "   GPR2: " + memory[(int)PCBptr + (int)GPR2Index] + "   GPR3: " + memory[(int)PCBptr + (int)GPR3Index]  + "   GPR4: " + memory[(int)PCBptr + (int)GPR4Index] + "   GPR5: " + memory[(int)PCBptr + (int)GPR5Index] + "   GPR6: " + memory[(int)PCBptr + (int)GPR6Index] + "   GPR7: " + memory[(int)PCBptr + (int)GPR7Index] + "\n");
    }//end of Print PCB function

    // ************************************************************
    // Function: Print Queue
    // Author(s): Vito
    // Task Description:
    // This function starts at the pointer and passes through the queue until end of list is reached
    // As each PCB is encountered, it's values are printed on screen using the PrintPCB() message.
    //
    // Input Parameters
    // Qptr, The memory location of the Queue
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // OK
    // ************************************************************
    public static long PrintQueue(long Qptr)
    {
        long currentPCBPtr = Qptr;

        if(currentPCBPtr == EndOfList){//If the initial address is EndOfList, then the list itself is empty.
            System.out.println("This list is empty");
            output = output + ("This list is empty\n");
            return OK;
        }
        // Walk thru the queue
        while (currentPCBPtr != EndOfList)
        {
            PrintPCB(currentPCBPtr); //Print PCB passing currentPCBPtr
            currentPCBPtr = memory[(int) currentPCBPtr + (int) NextPointerIndex]; //Set currentPCBPtr = next PCB pointer using currentPCBPtr
        } // end of while loop

        return  OK;
    }//end of PrintQueue

    // ************************************************************
    // Function: Insert into RQ
    // Author(s): Matthew
    // Task Description:
    // This function takes a PCB address pointed to by PCBptr inserts it into the ready queue.
    // it looks at a programs priority for where the new PCB will be inserted. All corresponding 'next pointer
    // index' values are adjusted upon insertion.
    //
    // Input Parameters
    // PCBptr points to location of PCB in memory
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // Error: invalid memory range
    // OK
    // ************************************************************
    public static long InsertIntoRQ(long PCBptr)
    {

        // Insert PCB according to Priority Round Robin algorithm
        // Use priority in the PCB to find the correct place to insert.
        long previousPtr = EndOfList;
        long currentPtr = RQ;

        // Check for invalid PCB memory address
        if(PCBptr < 0 || PCBptr > MAX_MEMORY_ADDRESS)
        {
            System.out.println("Error: Invalid memory range found. Returning error code -3");
            output = output + ("Error: Invalid memory range found. Returning error code -3");

            return INVALID_MEMORY_RANGE_ERROR;
        }

        memory[(int)PCBptr + (int)StateIndex] = ReadyState; //Set the PCB's state to "ready."
        memory[(int)PCBptr + (int)NextPointerIndex] = EndOfList; //Set the PCB's Next Pointer value to EndOfList.

        if(RQ == EndOfList) //If RQ is equal to the value of EndOfList (-1), then RQ is empty.
        {
            RQ = PCBptr;
            return OK;
        }
        // Walk thru RQ and find the place to insert
        // PCB will be inserted at the end of its priority

        while (currentPtr != EndOfList)
        {
            if(memory[(int)PCBptr + (int)PriorityIndex] > memory[(int)currentPtr + (int)PriorityIndex]) //If the priority of the PCB we want to insert is higher than the priority of the current PCB...
            {
                if(previousPtr == EndOfList) //If previousPtr is EndOfList, then the priority of the PCB that we want to insert is higher than the highest priority PCB.
                {
                    memory[(int)PCBptr + (int)NextPointerIndex] = RQ; //Change the current PCB's next pointer from EOL to the first PCB in the ready queue.
                    RQ = PCBptr; //Change the RQ value to the address of the PCB that we want to insert (because it is now the head of the queue).
                    return OK;
                }

                //If it isn't at the start of the RQ, then we're inserting this PCB into the middle of the list.
                memory[(int)PCBptr + (int)NextPointerIndex] = memory[(int)previousPtr + (int)NextPointerIndex]; //Set PCBptr's next pointer index to the previous pointer's next pointer index, because PCBptr is taking over the previous pointer's slot.
                memory[(int)previousPtr + (int)NextPointerIndex] = PCBptr; //Set the previous pointer's next pointer index to the PCB address, completing the insertion.
                return OK;
            }

            else //PCB to be inserted has lower or equal priority as the current PCB in RQ, move on to the next PCB.
            {
                previousPtr = currentPtr;
                currentPtr = memory[(int)currentPtr + (int)NextPointerIndex];
            }
        } // end of while loop
        // Insert PCB at the end of the RQ

        //If it gets to this point in the InsertIntoRQ() function, than the PCB we want to insert has the lowest priority in the RQ. Insert the new PCB into the end of the RQ.
        memory[(int)previousPtr + (int)NextPointerIndex] = PCBptr; //Change the previous pointer's next pointer index from EOL to the new PCB address. Note that the new PCB has a next pointer address of EOL.
        return OK;
    }//end of InsertIntoRQ

    // ************************************************************
    // Function: Insert into WQ
    // Author(s): Matthew
    // Task Description:
    // Insert the given PCB into waiting queue at the front of the queue. All pointer values are updated upon insertion
    //
    // Input Parameters
    // PCBptr points to location of PCB in memory
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // Error: invalid memory range
    // OK
    // ************************************************************
    public static long InsertIntoWQ (long PCBptr)
    {

        // Check for invalid PCB memory address
        if(PCBptr < 0 || PCBptr > MAX_MEMORY_ADDRESS)
        {
            System.out.println("Error: Invalid memory range found. Returning error code -3");
            output = output + ("Error: Invalid memory range found. Returning error code -3");
            return INVALID_MEMORY_RANGE_ERROR;
        }

        memory[(int)PCBptr + (int)StateIndex] = WaitingState; //Set the PCB's state to "waiting."
        memory[(int)PCBptr + (int)NextPointerIndex] = WQ; // set next pointer to end of list
        WQ = PCBptr;


        return OK;
    }//end of Insert Into Working queue

    // ************************************************************
    // Function: Select process from RQ
    // Author(s): Vito
    // Task Description:
    // selects the first process from the RQ and returns it, and also adjusts all PCB's accordingly
    //
    // Input Parameters
    // none
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // PCBptr
    // ************************************************************
    public static long SelectProcessFromRQ()
    {
        //Declare PCBptr as type long and initialize to RQ
        long PCBptr = RQ; //Set PCBptr to the first PCB in the ready queue.

        if(RQ != EndOfList) //If the ready queue is not empty...
        {
            RQ = memory[(int)RQ +(int) NextPointerIndex]; //Set RQ equal to the next 'PCB pointer value'.
        }

        //In try block catch to ArrayIndexOutOfBoundsException crash
        try{
            // Set next point to EOL in the PCB
            memory[(int)PCBptr + (int)NextPointerIndex] = EndOfList;
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }


        return PCBptr;
    }

    // ************************************************************
    // Function: SaveContext
    // Author(s): Vito
    // Task Description:
    // saves all content of currently running function within passed PCBptr, this way when the process returns to CPU
    // it can take place from where it last left off
    //
    // Input Parameters
    // PCBptr, the location of the PCB in memory
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    public static void SaveContext(long PCBptr)
    {
        //Copy all CPU GPRs into PCB using PCBptr with or without using loop
        memory[(int)PCBptr + (int)GPR0Index] = gpr[0];
        memory[(int)PCBptr + (int)GPR1Index] = gpr[1];
        memory[(int)PCBptr + (int)GPR2Index] = gpr[2];
        memory[(int)PCBptr + (int)GPR3Index] = gpr[3];
        memory[(int)PCBptr + (int)GPR4Index] = gpr[4];
        memory[(int)PCBptr + (int)GPR5Index] = gpr[5];
        memory[(int)PCBptr + (int)GPR6Index] = gpr[6];
        memory[(int)PCBptr + (int)GPR7Index] = gpr[7];
        memory[(int)PCBptr + (int)SPIndex] = SP; // Save SP
        memory[(int)PCBptr + (int)PCIndex] = PC; // Save PC
        memory[(int)PCBptr + (int)PSRIndex] = PSR;

    }//end of save context

    // ************************************************************
    // Function: Dispatcher
    // Author(s): Vito
    // Task Description:
    // this process takes the passed PCBptr and uses it to return all of the content
    // of the process within the PCBptr to the hardware components and restores its cpu context
    //
    // Input Parameters
    // PCBptr, location of PCB in memory
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    public static void Dispatcher(long PCBptr)
    {
        // Copy CPU GPR register values from given PCB into the CPU registers
        // This is opposite of save CPU context
        gpr[0] = memory[(int)PCBptr + (int)GPR0Index];
        gpr[1] = memory[(int)PCBptr + (int)GPR1Index];
        gpr[2] = memory[(int)PCBptr + (int)GPR2Index];
        gpr[3] = memory[(int)PCBptr + (int)GPR3Index];
        gpr[4] = memory[(int)PCBptr + (int)GPR4Index];
        gpr[5] = memory[(int)PCBptr + (int)GPR5Index];
        gpr[6] = memory[(int)PCBptr + (int)GPR6Index];
        gpr[7] = memory[(int)PCBptr + (int)GPR7Index];
        // Restore SP and PC from given PCB
        SP = memory[(int)PCBptr + (int)SPIndex];
        PC = memory[(int)PCBptr + (int)PCIndex];
        // Set system mode to User mode
        PSR = UserMode; // Set system mode to User mode.

    }//end of dispatcher

    // ************************************************************
    // Function: Terminate Process
    // Author(s): Eddy
    // Task Description:
    // Just like its name, this function terminates a process by taking a PCB pointer as a parameter, and
    // freeing the stack memory and PCB memory to be used by another process.
    //
    // Input Parameters
    // PCBptr, points to the PCB in memory
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    public static void TerminateProcess (long PCBptr)
    {

        FreeUserMemory(memory[(int)PCBptr + (int)StackStartAddrIndex], memory[(int)PCBptr + (int)StackSizeIndex]); // Return stack memory using stack start address and stack size in the given PCB.

        FreeOSMemory(PCBptr, PCBSize); // Return PCB memory using the PCBptr.

    }//end of TerminateProcess

    // ************************************************************
    // Function: Allocate OS Memory
    // Author(s): Eddy
    // Task Description:
    // This function takes a block out of OS memory to be used by the user.
    // size of the block to be taken depends on 'RequestedSize'. If the memory
    // allocation is successful, the memory address of the allocated block is returned. An error
    // message and code is returned otherwise.
    //
    // Input Parameters
    // RequestedSize, requested size of block for allocation
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // ERROR: The OSFreeList is empty, Return code = ErrorNoMem
    // ERROR: Requested memory size is too small. Return code = ErrorTooSmallMem
    // SUCCESS, Return code = currentPtr
    // ************************************************************
    //Allocate os memo
    public static long AllocateOSMemory(long RequestedSize)
    {
        if(OSFreeList == EndOfList)
        {
            System.out.println("ERROR: The OSFreeList is empty and there is no memory available to allocate. Returning error code -14");
            output = output + ("ERROR: The OSFreeList is empty and there is no memory available to allocate. Returning error code -14");
            // ErrorNoFreeMemory is constant set to < 0
            return ERROR_NO_MEM;

        }

        if(RequestedSize < 0){
            System.out.println("ERROR: The requested memory size is too small. When requesting memory, the request size must be greater than one. Returning error code -15");
            output = output + ("ERROR: The requested memory size is too small. When requesting memory, the request size must be greater than one. Returning error code -15");
            // ErrorInvalidMemorySize is constant < 0
            return ERROR_TOO_SMALL_MEM;
        }

        if(RequestedSize == 1){
            RequestedSize = 2; //min allocated memory size is 2 loctions
        }

        long currentPtr = OSFreeList; //Set the current pointer to the first free OS block.
        long previousPtr = EndOfList; //Set the previous pointer to EndOfList.

        while(currentPtr != EndOfList) //Check each block in the link list until block with requested memory size is found.
        {
            if(memory[(int)currentPtr+1] == RequestedSize) //If the size of the memory block is equal to the requested size, then we have found a block with requested size.
            {
                if(currentPtr == OSFreeList) //First block is the exact size requested.
                {
                    OSFreeList = memory[(int)currentPtr]; //Take the 'next OS block pointer' and set it equal to OSFreeList (which is the new start of the OSFreeList).
                    memory[(int)currentPtr] = EndOfList; //Adjust the 'next pointer' field of the current pointer block to equal EndOfList, since it's being returned.
                    return currentPtr; //Return the starting point of the block with the requested size.
                }
                else //The block found has exactly the size that is requested, but it is not the first block.
                {
                    memory[(int)previousPtr] = memory[(int)currentPtr]; //Set the pointer of the previous block to the pointer of the current block, effectively taking the desired block out of the OSFreeList.
                    memory[(int)currentPtr] = EndOfList; //Adjust the 'next pointer' field of the current pointer block to equal EndOfList, since it's being returned.
                    return currentPtr; //Return the starting point of the block with the requested size.
                }
            }

            else if(memory[(int)currentPtr + 1] > RequestedSize) //If the size of the memory block is greater than the size requested, then we can still use this block. Adjust all values to cut out what is needed.
            {
                if(currentPtr == OSFreeList) //First block size is the larger than the requested size.
                {
                    memory[(int)currentPtr + (int)RequestedSize] = memory[(int)currentPtr]; //Move next block pointer up a total of 'requestedSize' spaces.
                    memory[(int)currentPtr + (int)RequestedSize + 1] = memory[(int)currentPtr + 1] - RequestedSize; //Set the size of this next block to be the size that it was, minus the requestedSize (which was taken out).
                    OSFreeList = currentPtr + RequestedSize;  //Set the beginning of the list to the adjusted block value.
                    memory[(int)currentPtr] = EndOfList;  //Adjust the 'next pointer' field of the current pointer block to equal EndOfList, since it's being returned.
                    return currentPtr;	//Return the starting point of the block with the requested size.
                }
                else //Block size is the larger than the requested size and is not the first block.
                {
                    memory[(int)currentPtr + (int)RequestedSize] = memory[(int)currentPtr]; //Move next block pointer up a total of 'requestedSize' spaces.
                    memory[(int)currentPtr + (int)RequestedSize + 1] = memory[(int)currentPtr + 1] - RequestedSize; //Set the size of this next block to be the size that it was, minus the requestedSize (which was taken out).
                    memory[(int)previousPtr] = currentPtr + RequestedSize;  //Set the previous pointer's next block to the newly designed location.
                    memory[(int)currentPtr] = EndOfList;  //Adjust the 'next pointer' field of the current pointer block to equal EndOfList, since it's being returned.
                    return currentPtr;	//Return the starting point of the block with the requested size.
                }
            }

            else //The current block is too small, move onto the next block.
            {
                previousPtr = currentPtr; //Adjust previous pointer value.
                currentPtr = memory[(int)currentPtr]; //Adjust current pointer value.
            }
        }//end of while loop




        //If it makes it to this point, then there is not enough OS memory in a block available.
        System.out.println("ERROR: After traversing all OS free blocks, none of them was large enough for the requested size. Returning error code -1");
        output = output + ("ERROR: After traversing all OS free blocks, none of them was large enough for the requested size. Returning error code -1");

        return ERROR_NO_MEM;
    }// end of AllocateOSMemory

    // ************************************************************
    // Function: Free OS Memory
    // Author(s): Eddy
    // Task Description:
    // This function takes a location in memory and using the passed memory size
    // tries to free that memory area back into the OSFreeList.
    //
    // Input Parameters
    // ptr, pointer of block to free up memory
    // size, size of block to be freed up
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // ERROR: The memory address that you're trying to free is outside of the OSFreeList area, Return code = ErrorNoMemBlock
    // ERROR: The requested memory size is too small, Return code = RequestedMemoryTooSmall
    // ERROR: The memory address that you're trying to free is outside of the OSFreeList area, Return code = InvalidMemoryRange
    // SUCCESS: Memory Free'd. Return code = OK
    // ************************************************************
    public static long FreeOSMemory(long ptr, long size)
    {
        if(ptr < START_ADDRESS_OF_OS_FREE_LIST || ptr > MAX_MEMORY_ADDRESS) //If the pointer given is out of range...
        {

            System.out.println("\nERROR: The memory address that you're trying to free is outside of the OSFreeList area. Invalid memory address. Returning error code -16.");
            output = output + ("\nERROR: The memory address that you're trying to free is outside of the OSFreeList area. Invalid memory address. Returning error code -16.");
            return ERROR_NO_MEM_BLOCK;
        }

        if(size == 1) // check for minimum allocated size, which is 2 even if user asks for 1 location
        {
            size = 2; //Minimum allocated memory size allowed is 2 locations.
        }
        else if(size < 1) //Size to OS memory to free is too small, return error.
        {
            System.out.println("\nERROR: The requested memory size is too small. When freeing memory, the request size must be greater than one. Returning error code -17.");
            output = output + ("\nERROR: The requested memory size is too small. When freeing memory, the request size must be greater than one. Returning error code -17.");
            return REQUESTED_MEMORY_TOO_SMALL;
        }
        else if((ptr+size) > MAX_MEMORY_ADDRESS) //Trying to free elements in memory that pass its' limit, return error.
        {
            System.out.println("\nERROR: The memory address that you're trying to free is outside of the OSFreeList area. Invalid memory address. Returning error code -3.");
            output = output + ("\nERROR: The memory address that you're trying to free is outside of the OSFreeList area. Invalid memory address. Returning error code -3.");
            return INVALID_MEMORY_RANGE_ERROR;
        }

        memory[(int) ptr] = OSFreeList; //Set the pointer of the released free block to point to the 'front' of the OSFreeList (the newly released block is taking it's place at the front).
        memory[(int) (ptr + 1)] = size; //Set the size of this block in OSFreeList to the size given.
        OSFreeList = ptr; //Set the pointer given to be the new front of the OSFreeList.
        return OK;

    }  // End of FreeOSMemory function.

    // ************************************************************
    // Function: Allocate User Memory
    // Author(s): Vito
    // Task Description:
    // This function takes a block equal to the requested size from user memory so it can be used by the user.
    // and returns the pointer to the memory block on successful execution.
    //
    // Input Parameters
    // requested size, size of memory in user memory to be allocated.
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // ERROR: The UserFreeList is empty, Return code = ErrorNoMem
    // ERROR: The requested memory size is too small, Return code = RequestedMemoryTooSmall
    // SUCCESS: Return code = currentPtr
    // ************************************************************
    public static long AllocateUserMemory(long RequestedSize)  // return value contains address or error code
    {
        if(UserFreeList == EndOfList) //If UserFreeList is equal to EndOfList, then there is no memory available to allocate.
        {
            System.out.println("\nERROR: The UserFreeList is empty and there is no memory available to allocate. Returning error code -2.");
            output = output + ("\nERROR: The UserFreeList is empty and there is no memory available to allocate. Returning error code -2.");
            return ERROR_NO_MEM;
        }

        if(RequestedSize < 0)
        {
            System.out.println("\nERROR: The requested memory size is too small. When requesting memory, the request size must be greater than one. Returning error code -17.");
            output = output + ("\nERROR: The requested memory size is too small. When requesting memory, the request size must be greater than one. Returning error code -17.");
            return REQUESTED_MEMORY_TOO_SMALL;
        }

        if(RequestedSize == 1) // check for minimum allocated size, which is 2 even if user asks for 1 location
        {
            RequestedSize = 2; //Minimum allocated memory size allowed is 2 locations.
        }

        long currentPtr = UserFreeList; //Set the current pointer to the first free User block.
        long previousPtr = EndOfList; //Set the previous pointer to EndOfList.

        while(currentPtr != EndOfList) //Check each block in the link list until block with requested memory size is found.
        {
            if(memory[(int) (currentPtr+1)] == RequestedSize) //If the size of the memory block is equal to the requested size, then we have found a block with requested size.
            {
                if(currentPtr == UserFreeList) //First block is the exact size requested.
                {
                    UserFreeList = memory[(int) currentPtr]; //Take the 'next User block pointer' and set it equal to UserFreeList (which is the new start of the UserFreeList).
                    memory[(int) currentPtr] = EndOfList; //Adjust the 'next pointer' field of the current pointer block to equal EndOfList, since it's being returned.
                    return currentPtr; //Return the starting point of the block with the requested size.
                }
                else //The block found has exactly the size that is requested, but it is not the first block.
                {
                    memory[(int) previousPtr] = memory[(int) currentPtr]; //Set the pointer of the previous block to the pointer of the current block, effectively taking the desired block out of the UserFreeList.

                    memory[(int) currentPtr] = EndOfList; //Adjust the 'next pointer' field of the current pointer block to equal EndOfList, since it's being returned.
                    return currentPtr; //Return the starting point of the block with the requested size.
                }
            }

            else if(memory[(int) (currentPtr + 1)] > RequestedSize) //If the size of the memory block is greater than the size requested, then we can still use this block. Adjust all values to cut out what is needed.
            {
                if(currentPtr == UserFreeList) //First block size is the larger than the requested size.
                {
                    memory[(int) (currentPtr + RequestedSize)] = memory[(int) currentPtr]; //Move next block pointer up a total of 'requestedSize' spaces.
                    memory[(int) (currentPtr + RequestedSize + 1)] = memory[(int) (currentPtr + 1)] - RequestedSize; //Set the size of this next block to be the size that it was, minus the requestedSize (which was taken out).
                    UserFreeList = currentPtr + RequestedSize;  //Set the beginning of the list to the adjusted block value.
                    memory[(int) currentPtr] = EndOfList;  //Adjust the 'next pointer' field of the current pointer block to equal EndOfList, since it's being returned.
                    return currentPtr;	//Return the starting point of the block with the requested size.
                }
                else //Block size is the larger than the requested size and is not the first block.
                {
                    memory[(int) (currentPtr + RequestedSize)] = memory[(int) currentPtr]; //Move next block pointer up a total of 'requestedSize' spaces.
                    memory[(int) (currentPtr + RequestedSize + 1)] = memory[(int) (currentPtr + 1)] - RequestedSize; //Set the size of this next block to be the size that it was, minus the requestedSize (which was taken out).
                    memory[(int) previousPtr] = currentPtr + RequestedSize;  //Set the previous pointer's next block to the newly designed location.
                    memory[(int) currentPtr] = EndOfList;  //Adjust the 'next pointer' field of the current pointer block to equal EndOfList, since it's being returned.
                    return currentPtr;	//Return the starting point of the block with the requested size.
                }
            }

            else //The current block is too small, move onto the next block.
            {
                previousPtr = currentPtr; //Adjust previous pointer value.
                currentPtr = memory[(int) currentPtr]; //Adjust current pointer value.
            }
        }

        //If it makes it to this point, then there is not enough OS memory in a block available.
        System.out.println("\nERROR: After traversing all User free blocks, none of them was large enough for the requested size. Returning error code -2.");
        output = output + ("\nERROR: After traversing all User free blocks, none of them was large enough for the requested size. Returning error code -2.");

        return ERROR_NO_MEM;

    }  // End of AllocateUserMemory function.

    // ************************************************************
    // Function: Free User Memory
    // Author(s): Vito
    // Task Description:
    // This function takes a pointer and memory size and attempts to free the pointed area in memory back into
    // the user free memory area
    //
    // Input Parameters
    // ptr, points to block to be freed
    // size, size of block to be freed
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // ERROR: The memory address that you're trying to free is outside of the UserFreeList area, Return code = ErrorNoMemBlock
    // ERROR: The requested memory size is too small, Return code = RequestedMemoryTooSmall
    // ERROR: The memory address that you're trying to free is outside of the UserFreeList area, Return code = InvalidMemoryRange
    // SUCCESS: Return code = OK
    // ************************************************************
    public static long FreeUserMemory(long ptr, long size)  // return value contains OK or error code
    {
        System.out.println("Ptr is : " + ptr);
        output = output + ("Ptr is : " + ptr);
        if(ptr < START_ADDRESS_OF_USER_FREE_LIST || ptr > END_ADDRESS_OF_USER_FREE_LIST) //If the pointer given is out of range...
        {
            System.out.println("\nERROR: The memory address that you're trying to free is outside of the UserFreeList area. Invalid memory address. Returning error code -16.");
            output = output + ("\nERROR: The memory address that you're trying to free is outside of the UserFreeList area. Invalid memory address. Returning error code -16.");
            return ERROR_NO_MEM_BLOCK;
        }

        if(size == 1)
        {
            size = 2; //Minimum allocated memory size allowed is 2 locations.
        }
        else if(size < 1) //Size to User memory to free is too small, return error.
        {
                System.out.println("\nERROR: The requested memory size is too small. When freeing memory, the request size must be greater than one. Returning error code -17.");
                output = output + ("\nERROR: The requested memory size is too small. When freeing memory, the request size must be greater than one. Returning error code -17.");

            return REQUESTED_MEMORY_TOO_SMALL;
        }
        else if((ptr+size) > MAX_MEMORY_ADDRESS) //Trying to free elements in memory that pass its' limit, return error.
        {
            System.out.println("\nERROR: The memory address that you're trying to free is outside of the UserFreeList area. Invalid memory address. Returning error code -3.");
            output = output + ("\nERROR: The memory address that you're trying to free is outside of the UserFreeList area. Invalid memory address. Returning error code -3.");

            return INVALID_MEMORY_RANGE_ERROR;
        }

        memory[(int) ptr] = UserFreeList; //Set the pointer of the released free block to point to the 'front' of the UserFreeList (the newly released block is taking it's place at the front).
        memory[(int) (ptr + 1)] = size; //Set the size of this block in UserFreeList to the size given.
        UserFreeList = ptr; //Set the pointer given to be the new front of the UserFreeList.
        return OK;

    }  // End of FreeUserMemory function.

    // ***********************************************************
    // Function: Check and Process Interrupt
    // Author(s): Matthew
    // Task Description:
    // This function checks if an interrupt has been encountered, when called, it presents the user with 5
    // options to select from. Depending on the selected option the proper response is processed.
    //
    // Input Parameters
    // none
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // SUCCESS: Return code = InterruptID
    // ************************************************************
    public static long CheckAndProcessInterrupt() throws IOException
    {

        long InterruptID = -1;
        boolean IncorrectInputFlag = false;

        while(!IncorrectInputFlag) //Flag used to loop if incorrect interrupt value is encountered (value that is not 0-4).
        {
            // Prompt and read interrupt ID
            System.out.println("\nInterrupt detected, what type of interrupt has occurred? Types of interrupts:");
            output = output + ("\nInterrupt detected, what type of interrupt has occurred? Types of interrupts:");
            System.out.println("0 – No interrupt."); // 0 – no interrupt
            output = output + ("\n0 – No interrupt.");
            System.out.println("1 – Run program."); // 1 – run program
            output = output + ("\n1 – Run program.");
            System.out.println("2 – Shutdown system."); // 2 – shutdown system
            output = output + ("\n2 – Shutdown system.");
            System.out.println("3 – Input operation completion (io_getc)."); // 3 – Input operation completion (io_getc)
            output = output + ("\n3 – Input operation completion (io_getc).");
            System.out.println("4 – Output operation completion (io_putc)."); // 4 – Output operation completion (io_putc)
            output = output + ("\n4 – Output operation completion (io_putc).");
            System.out.println();
            output = output + ("\n\n");
            System.out.print("Interrupt ID:");
            output = output + ("Interrupt ID:");
            Scanner input = new Scanner(System.in);
            InterruptID = input.nextLong();
            output = output + InterruptID;
            System.out.println();
            output = output + ("\n");
            System.out.println("Interrupt that was entered: " + InterruptID);
            output = output + ("\nInterrupt that was entered: " + InterruptID);


            switch((int)InterruptID)
            {
                case NoInterrupt: //'No Interrupt' (0) was entered.
                {
                    IncorrectInputFlag = true; //Adjust flag value to break loop.
                    break;
                }

                case RunProgramInterrupt: //'Run Program' (1) was entered.
                {
                    ISRrunProgramInterrupt();
                    IncorrectInputFlag = true; //Adjust flag value to break loop.
                    break;
                }

                case ShutdownSystemInterrupt: //'Shutdown System' (2) was entered.
                {
                    ISRshutdownSystem();
                    systemShutdownStatus = true; //Adjust global shutdown status.
                    IncorrectInputFlag = true; //Adjust flag value to break loop.
                    break;
                }

                case IO_GETCInterrupt: //'Input Operation Completion (io_getc)' (3) was entered.
                {
                    ISRinputCompletionInterrupt();
                    IncorrectInputFlag = true; //Adjust flag value to break loop.
                    break;
                }

                case IO_PUTCInterrupt: //'Output Operation Completion (io_putc)' (4) was entered.
                {
                    ISRoutputCompletionInterrupt();
                    IncorrectInputFlag = true; //Adjust flag value to break loop.
                    break;
                }

                default: //Invalid interrupt ID.
                {
                    System.out.println("\nInvalid interrupt ID entered. Try again.\n");
                    output = output + ("\n\nInvalid interrupt ID entered. Try again.\n");
                    break;
                }
            }//end of switch
        }//end of while

        return InterruptID; //Returns the interrupt ID.


    }// End of CheckAndProcessInterrupt function.

    //***********************************************************
    // Function: ISR Program Interrupt
    // Author(s): Vito
    // Task Description:
    // This function works as an interrupt service request for the run program request. The function prompts
    // the user for a program to run, and then creates a process for that program.
    //
    // Input Parameters
    // none
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    public static void ISRrunProgramInterrupt() throws IOException
    {
        System.out.println("\nRun program interrupt has been encountered. Please enter the name of the program to run (name of the machine language program): ");
        output = output + ("\n\nRun program interrupt has been encountered. Please enter the name of the program to run (name of the machine language program): ");
        Scanner input = new Scanner(System.in);
        String programToRun = input.nextLine();
        output = output + (programToRun);
        CreateProcess(programToRun, DefaultPriority); //Call Create Process passing filename and Default Priority as arguments.

    }  // End of ISRrunProgram function.

    //***********************************************************
    // Function: ISR Input Completion Interrupt
    // Author(s): Matthew
    // Task Description:
    // This function works as an interrupt service request for the input completion request. The function is used to handle
    // the IO_GETC interrupt by reading a PID, searching the WQ for the matching  PCB and stores the input into the GPR1 register
    //
    // Input Parameters
    // none
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    public static void ISRinputCompletionInterrupt()
    {
        long PID;
        char inputCharacter;
        System.out.println("ISR designed for input completion has begun running, please specify the PID of the process that the input is being completed for: ");
        output = output + ("\nISR designed for input completion has begun running, please specify the PID of the process that the input is being completed for: ");
        Scanner input = new Scanner(System.in);
        PID = input.nextLong(); //Read the PID of the process we're completing input for.

        long PCBPtr = SearchAndRemovePCBfromWQ(PID); //Search WQ to find the PCB that has the given PID, return value is stored in PCBPtr.
        if(PCBPtr > 0) //Only performs this section with a valid PCB address.
        {
            System.out.println("Please enter a character to store: ");
            output = output + ("\nPlease enter a character to store: ");

            inputCharacter = input.next().charAt(0); //Read one character from standard input device keyboard.

            memory[(int) (PCBPtr + GPR1Index)] = (long) inputCharacter; //Store the character in the GPR in the PCB. Use typecasting from char to long data types.
            memory[(int) (PCBPtr + StateIndex)] = ReadyState; //Set process state to Ready in the PCB.
            System.out.println("The character " + inputCharacter + " was successfully INPUTTED.");
            output = output + ("\nThe character " + inputCharacter + " was successfully INPUTTED.");
            InsertIntoRQ(PCBPtr); //Insert PCB into ready queue.
        }

    }  // End of ISRinputCompletionInterrupt function.

    //***********************************************************
    // Function: ISR Output Completion Interrupt
    // Author(s): Matthew
    // Task Description:
    // This function works as an interrupt service request for the Output completion request. The function handles
    // the IO_PUTC interrupt by reading a PID, searching the WQ for the matching PCB, and printing the character stored
    // in the PCB's GPR1
    //
    // Input Parameters
    // none
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    public static void ISRoutputCompletionInterrupt()
    {
        long PID;
        char outputcharacter;
        System.out.println("ISR designed for output completion has begun running, please specify the PID of the process that the output is being completed for: ");
        output = output + ("\nISR designed for output completion has begun running, please specify the PID of the process that the output is being completed for: ");
        Scanner input = new Scanner(System.in);
        PID = input.nextLong(); //Read the PID of the process we're completing input for.

        long PCBPtr = SearchAndRemovePCBfromWQ(PID); //Search WQ to find the PCB that has the given PID, return value is stored in PCBPtr.
        if(PCBPtr > 0) //Only performs this section with a valid PCB address.
        {
            outputcharacter = (char) memory[(int) (PCBPtr + GPR1Index)]; //Typecast the ascii code for the output character back into a character value. Store in output character.
            System.out.println("\nOUTPUT COMPLETED, CHARACTER DISPLAYED: " + outputcharacter);//Print the character that was in the PCB's GPR1 slot.
            output = output + ("\n\nnOUTPUT COMPLETED, CHARACTER DISPLAYED: " + outputcharacter);
            memory[(int) (PCBPtr + StateIndex)] = ReadyState; //Set process state to Ready in the PCB.
            InsertIntoRQ(PCBPtr); //Insert PCB into ready queue.
        }

    }  // End of ISRonputCompletionInterrupt function.

    //***********************************************************
    // Function: ISR Shutdown System
    // Author(s): Eddy
    // Task Description:
    // This function works as an interrupt service request for the shutdown system request. The function
    // will terminate all processes in the WQ and RQ one by one and then changes the shutdownsystem status to true inside
    // the checkandprocessinterrupt function.
    //
    // Input Parameters
    // none
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // none
    // ************************************************************
    public static void ISRshutdownSystem()
    {
        //Terminate all processes in RQ one by one.
        long ptr = RQ; //Set ptr to first PCB pointed by RQ.

        while(ptr != EndOfList) //While there are still PCBs in the RQ...
        {
            RQ = memory[(int) (ptr + NextPointerIndex)]; //Set RQ to equal the next PCB in RQ.
            TerminateProcess(ptr); //Terminate the current process in the list.
            ptr = RQ; //Set ptr to the next PCB in RQ.
        }

        //Terminate all processes in WQ one by one.
        ptr = WQ; //Set ptr to first PCB pointed by WQ.

        while(ptr != EndOfList) //While there are still PCBs in the WQ...
        {
            WQ = memory[(int) (ptr + NextPointerIndex)]; //Set RQ to equal the next PCB in RQ.
            TerminateProcess(ptr); //Terminate the current process in the list.
            ptr = WQ; //Set ptr to the next PCB in WQ.
        }

    }  // End of ISRshutdownSystem function.

    //***********************************************************
    // Function: Search And Remove PCB From WQ
    // Author(s): Eddy
    // Task Description:
    // This function takes a pid and will attempt to remove the PCB
    // that is tied to that pid from the waiting queue.
    //
    // Input Parameters
    // pid, process id of PCB to be removed
    //
    // Output Parameters
    // None
    //
    // Function Return Value
    // ERROR: Invalid PID used, Return code = ER_PID
    // ERROR: PID not found, Return code = ER_PIDNotFound
    // SUCCESS: Removal was successful. Return code = PCB Address of PCB with corresponding PID
    // ************************************************************
    public static long SearchAndRemovePCBfromWQ (long pid)
    {

        long currentPCBptr = WQ;
        long previousPCBptr = EndOfList;

        if(pid < 1) //PID cannot be zero or less than zero. Check for an incorrect PID.
        {
            System.out.println("\nInvalid PID used. Returning error code -18.\n");
            output = output + ("\n\nInvalid PID used. Returning error code -18.\n");
            return ER_PID;
        }

        //Search WQ for a PCB that has the given pid. If a match is found, remove it from WQ and return the PCB pointer
        while(currentPCBptr != EndOfList)
        {
            if(memory[(int) (currentPCBptr + PIDIndex)] == pid) //If the current pointer's PID matches the PID we're looking for, then a match is found. Remove that process from WQ.
            {
                if(previousPCBptr == EndOfList) //First PCB in WQ is a match.
                {
                    WQ = memory[(int) (currentPCBptr + NextPointerIndex)]; //Set the starting point of WQ to the second PCB in WQ.
                }
                else //Match is somewhere in the middle of WQ.
                {
                    memory[(int) (previousPCBptr + NextPointerIndex)] = memory[(int) (currentPCBptr + NextPointerIndex)]; //Adjust the previous PCB's next pointer index to be the next pointer index of the PCB that's being removed from WQ.
                }

                memory[(int) (currentPCBptr + NextPointerIndex)] = EndOfList; //Adjust the returning PCB's next pointer index to be 'EndOfList'.
                return currentPCBptr; //Return matching PCB.
            }

            previousPCBptr = currentPCBptr; //Move on to the next PCB if there is no match.
            currentPCBptr = memory[(int) (currentPCBptr + NextPointerIndex)];
        }
        System.out.println("\nAfter traversing the waiting queue, a process with the specified ID: " + pid + " could not be found. Returning error code -18.\n");
        output = output + ("\n\nAfter traversing the waiting queue, a process with the specified ID: " + pid + " could not be found. Returning error code -18.\n");

        return ER_PIDNotFound;

    }  // End of SearchAndRemovePCBfromWQ function.

    // ************************************************************
    // Function: SystemCall
    // Author(s): Vito
    // Task Description:
    // This process takes a system call ID and checks it  to get the corresponding response
    //
    // Input Parameters
    // none
    // Output Parameters
    // None
    //
    // Function Return Value
    // ERROR: Invalid system call ID encountered. Return code = ER_ISC
    // ERROR: Incorrect memory size specified. Return code = IncorrectSizeValue
    // ERROR: Not enough memory available. Return code = ErrorNoMem
    // ERROR: Requested size too small. Return code = ErrorTooSmallMem
    // ERROR: Memory address that is attempting to be freed is out of range. Return code = InvalidMemoryRange
    // SUCCESS: System Call was successfully processed. Return code = status
    // ************************************************************
    public static long SystemCall(long SystemCallID)
    {
        PSR = OSMode; //Set system mode to OS mode.

        long status = OK; //Declare long status set to OK.

        switch((int)SystemCallID)
        {
            case Process_Create: //Create Process – User process is creating a child process. NOT implemented in this project.
            {
                System.out.println("\nCreate process system call not implemented." );
                output = output + ("\n\nCreate process system call not implemented.");

                //Display 'create process system call not implemented'.
                break;
            }

            case Process_Delete: //Delete Process - NOT implemented in this project.
            {
                System.out.println("\nDelete process system call not implemented.");
                output = output + ("\n\nDelete process system call not implemented.");


                //Display 'delete process system call not implemented'.
                break;
            }

            case Process_Inquiry: //Process Inquiry - NOT implemented in this project.
            {
                System.out.println("\nProcess inquiry system call not implemented.");
                output = output + ("\n\nProcess inquiry system call not implemented.");

                //Display 'process inquiry system call not implemented'.
                break;
            }

            case Mem_Alloc: //Dynamic memory allocation - Allocate user free memory system call.
            {


                status = MemAllocSystemCall();
                break;
            }

            case Mem_Free: //Free dynamically allocated user memory system call.
            {


                status = MemFreeSystemCall();
                break;
            }

            case Msg_Send: //Message Send - NOT implemented in this project.
            {

                System.out.println("\nMessage send system call not implemented.");
                output = output + ("\n\nMessage send system call not implemented.");


                break;
            }

            case Msg_Receive: //Message Receive - NOT implemented in this project.
            {
                System.out.println("\nMessage receive system call not implemented.");
                output = output + ("\n\nMessage recieved system call not implemented.");


                //Display 'message receive system call not implemented'.
                break;
            }

            case IO_GETC: // IO_GETC - Input a single character.
            {
                status = io_getcSystemCall();
                break;
            }

            case IO_PUTC: // IO_PUTC - Output a single character.
            {
                status = io_putcSystemCall();
                break;
            }

            case Time_Get: //Get time - NOT implemented in this project.
            {
                System.out.println("\nGet time system call not implemented.");
                output = output + ("\n\nGet time system call not implemented.");


                //Display 'get time system call not implemented'.
                break;
            }

            case Time_Set: //Set time - NOT implemented in this project.
            {
                System.out.println("\nSet time system call not implemented.");
                output = output + ("\n\nSet time system call not implemented.");

                //Display 'set time system call not implemented'.
                break;
            }

            default: // Default switch value set to 'invalid system call ID'.
            {
                System.out.println("\nThe system call ID is invalid. Returning error code -20.");
                output = output + ("\n\nThe system call ID is invalid. Returning error code -20.");


                //Display invalid system call ID error message.
                return ER_ISC; //Return invalid system call.
            }
        }

        PSR = UserMode; // Set system mode to user mode.

        return status;
    }//end of SystemCall

    //***********************************************************
    // Function: Memory Alloc SystemCall
    // Author(s): Matthew
    // Task Description:
    // This function serves as a system call for the MTOPS operating system. Once a SYSTEMCALL
    // Opcode is found in a machine language program, the CPU calls the SystemCall() function
    // and passes the appropriate system call. When a 4 is passed passed this function uses the
    // value stored in GPR2 as a size value. This value represents how much memory we would like
    // to allocate from user memory.
    //
    // Input Parameters
    //  GPR2, Required size of memory.
    //
    // Output Parameters
    // GPR0, Return code
    // GPR1, Start address of the allocated memory.
    //
    // Function Return Value
    // ERROR: size of memory requested to be freed was out of range Return code = IncorrectSizeValue
    // ERROR: Not enough memory available. Return code = ErrorNoMem
    // ERROR: Requested size too small. Return code = RequestedMemoryTooSmall
    // SUCCESS: Memory allocation was successful. Return value stored in = GPR1
    // ************************************************************
    public static long MemAllocSystemCall()
    {
        long size = gpr[2]; //Declare long 'size' and set it to GPR2 value. GPR[2] is GPR2.

        if (size < 1 || size > StartSizeOfUserFreeList) //Size cannot be negative or 0. Size cannot be greater than 2000.
        {
            System.out.println("\nThe size of memory that was requested to be freed was out of range (either too big or too small). Returning error code -21.\n");
            output = output + ("\n\nThe size of memory that was requested to be freed was out of range (either too big or too small). Returning error code -21.\n");

            return IncorrectSizeValue;
        }

        if (size == 1)
        {
            size = 2; //Minimum allocated memory size allowed is 2 locations.
        }

        gpr[1] = AllocateUserMemory(size); //Allocate user memory, passing 'size' as an argument. Store the return value in GPR1.

        if(gpr[1] < 0)
        {
            gpr[0] = gpr[1]; //If this condition is hit, then we store the value in GPR1, which is an error message, into GPR0. GPR0 holds the status of the call.
        }
        else
        {
            gpr[0] = OK; //If this condition is hit, then there was no error. The GPR0 value will be okay, and GPR1 will have an accurate starting memory location.
        }

        System.out.println("Mem_Alloc system call encountered. The final values of GPR0, GPR1, and GPR2 are: GPR0 = " + gpr[0] + "  GPR1 = " + gpr[1] + "  GPR2 = " + gpr[2]);
        output = output + ("\n\nMem_Alloc system call encountered. The final values of GPR0, GPR1, and GPR2 are: GPR0 = " + gpr[0] + "  GPR1 = " + gpr[1] + "  GPR2 = " + gpr[2]);

        //Display Mem_alloc system call, and parameters GPR0, GPR1, GPR2.

        return gpr[0]; //Return the status of the memory allocation.

    }  // End of MemAllocaSystemCall function.

    //***********************************************************
    // Function: Memory Free SystemCall
    // Author(s): Vito
    // Task Description:
    // This function serves as a system call for the MTOPS operating system. Once a SYSTEMCALL
    // Opcode is found in a machine language program, the CPU calls the SystemCall() function
    // and passes the appropriate system call. When a 5 is passed this function uses the
    // value stored in GPR2 as a size value and the value stored in GPR1 as a starting address.
    // the status of MemFree is stored in GPr0
    //
    // Input Parameters
    // GPR1 pointer to memory block being freed
    // GPR2 size of memory to be freed.
    //
    // Output Parameters
    // GPR0, Return code
    //
    // Function Return Value
    // ERROR: size of memory requested to be freed was out of range Return code = IncorrectSizeValue
    // ERROR: Not enough memory available. Return code = ErrorNoMem
    // ERROR: Requested size too small. Return code = RequestedMemoryTooSmall
    // SUCCESS: Memory allocation was successful. Return code = OK
    // ************************************************************
    public static long MemFreeSystemCall()
    {
        long size = gpr[2]; //Declare long 'size' setting it to GPR2 value.

        if (size < 1 || size > StartSizeOfUserFreeList) //Size cannot be negative or 0. Size cannot be greater than 2000.
        {
            System.out.println("\nThe size of memory that was requested to be freed was out of range (either too big or too small). Returning error code -21.\n" );
            output = output + ("\n\nThe size of memory that was requested to be freed was out of range (either too big or too small). Returning error code -21.\n");

            return IncorrectSizeValue;
        }

        if (size == 1)
        {
            size = 2; //Minimum allocated memory size allowed is 2 locations.
        }

        gpr[0] = FreeUserMemory(gpr[1], size); //Free the desired user memory, passing GPR1 (which has the starting address) and 'size' as an argument. Store the return value in GPR0.
        System.out.println("\nMem_Free system call encountered. The final values of GPR0, GPR1, and GPR2 are: GPR0 = " + gpr[0] + "  GPR1 = " + gpr[1] + "  GPR2 = " + gpr[2]);
        output = output + ("\n\nMem_Free system call encountered. The final values of GPR0, GPR1, and GPR2 are: GPR0 = " + gpr[0] + "  GPR1 = " + gpr[1] + "  GPR2 = " + gpr[2]);

        //Display Mem_free system call, and parameters GPR0, GPR1, GPR2.

        return gpr[0]; //Return the status of the memory freeing.

    }  // End of MemAllocaSystemCall function.

    //***********************************************************
    // Function: IO_GETC SystemCall
    // Author(s): Matthew
    // Task Description:
    // This function serves as a system call for the MTOPS operating system. Once a SYSTEMCALL
    // Opcode is found in a machine language program, the CPU calls the SystemCall() function
    // and passes the appropriate system call. When an 8 is passed this function leaves the cpu with an IO_GETC interrupt
    //
    // Input Parameters
    // none
    //
    // Output Parameters
    // none
    //
    // Function Return Value
    // SUCCESS, Return value = IO_GETCINTERRUPT
    // ************************************************************
    public static long io_getcSystemCall()
    {
        System.out.println("\nInput operation required, leaving CPU for input interrupt.\n");
        output = output + ("\n\nInput operation required, leaving CPU for input interrupt.\n");
        return IO_GETCInterrupt;//Return start of input operation event code (3).

    }  // End of io_getcSystemCall function.

    //***********************************************************
    // Function: IO_PUTC SystemCall
    // Author(s): Matthew
    // Task Description:
    // This function serves as a system call for the MTOPS operating system. Once a SYSTEMCALL
    // Opcode is found in a machine language program, the CPU calls the SystemCall() function
    // and passes the appropriate system call. When a 9 is passed this function leaves the cpu with an IO_PUTC interrupt
    //
    // Input Parameters
    // none
    //
    // Output Parameters
    // none
    //
    // Function Return Value
    // SUCCESS, Return value = IO_PUTCINTERRUPT
    // ************************************************************
    public static long io_putcSystemCall()
    {
        System.out.println("\nOutput operation required, leaving CPU for output interrupt.\n");
        output = output + ("\n\nOutput operation required, leaving CPU for output interrupt.\n");

        return IO_PUTCInterrupt; //Return start of output operation event code (4).

    }  // End of io_putcSystemCall function.

}    //end of HypoMachine Class
