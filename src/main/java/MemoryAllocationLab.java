import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory;
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    public static void processRequests(String filename) {
        memory = new ArrayList<>();
        try (BufferedReader b = new BufferedReader(new FileReader(filename)))
        {
        String next = b.readLine();
        if (next == null)
        {
          System.out.println("Couldn't find total memory to allocate to buffer");
          return;
        }
        totalMemory = Integer.parseInt(next.trim());
        memory.add(new MemoryBlock(0, totalMemory, null));
        System.out.println("Total memory: " + totalMemory);
        System.out.println("\nProcessing requests...\n");
        
        String[] command;
        String process;
        int memoryNeed;
        while ((next = b.readLine()) != null)
        {
          command = next.trim().split(" ");
          if (next.contains("REQUEST") && command.length == 3)
          {
            process = command[1];
            memoryNeed = Integer.parseInt(command[2]);
            allocate(process, memoryNeed);
          }
          else if (next.contains("RELEASE") && command.length == 2)
          {
            process = command[1];
            deallocate(process);
          }
          else
          {
            System.out.println("Error: file line '" + next + "'couldn't be read, will continue");
          }
          
        }
        }
        catch (NumberFormatException e)
        {
          System.out.println("Incorrect number formatting in file");
        }
        catch (IOException e)
        {
          System.out.println("Couldn't find file " + filename);
        }
    }

    private static void allocate(String processName, int size) {
        for(int i = 0; i < memory.size(); i++)
        {
          if (memory.get(i).isFree() && memory.get(i).size >= size)
          {
              int remaining = memory.get(i).size - size;
              memory.get(i).size = size;
              memory.get(i).processName = processName;
              if (remaining > 0)
                memory.add(i+1, new MemoryBlock(memory.get(i).getEnd() + 1, remaining, null));
              successfulAllocations++;
              System.out.println("REQUEST " + processName + " " + size + " KB -> Success");
              return;
          }
        }
        failedAllocations++;
        System.out.println("Error: Insufficient memory, couldn't allocate " + processName);
    }
    
    public static void deallocate(String processName) {
      for (int i = 0; i < memory.size(); i++)
      {
        if (!memory.get(i).isFree() && memory.get(i).processName.equals(processName))
        {
          memory.get(i).processName = null;
          System.out.println("RELEASE " + processName + " -> Success");
          //merge free spaces
          if (memory.get(i+1).isFree())
          {
            memory.get(i).size += memory.get(i+1).size;
            memory.remove(i+1);
          }
          if (i > 0 && memory.get(i-1).isFree())
          {
            memory.get(i-1).size += memory.get(i).size;
            memory.remove(i);
          }
          return;
        }
      }
      System.out.println("Error: Couldn't find and release " + processName);
    }
    
    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        for (MemoryBlock block : memory) {
            String status = block.isFree() ? "FREE" : block.processName;
            String allocated = block.isFree() ? "" : " - ALLOCATED";
            System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n",
                    blockNum++,
                    block.start,
                    block.getEnd(),
                    " ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())),
                    status,
                    block.size,
                    allocated);
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");

        int allocatedMem = 0;
        int freeMem = 0;
        int numProcesses = 0;
        int numFreeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock block : memory) {
            if (block.isFree()) {
                freeMem += block.size;
                numFreeBlocks++;
                largestFree = Math.max(largestFree, block.size);
            } else {
                allocatedMem += block.size;
                numProcesses++;
            }
        }

        double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
        double freePercent = (freeMem * 100.0) / totalMemory;
        double fragmentation = freeMem > 0 ?
                ((freeMem - largestFree) * 100.0) / freeMem : 0;

        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
        System.out.printf("Number of Processes:    %d\n", numProcesses);
        System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    /**
     * Main method (FULLY PROVIDED)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MemoryAllocationLab <input_file>");
            System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
            return;
        }

        System.out.println("========================================");
        System.out.println("Memory Allocation Simulator (First-Fit)");
        System.out.println("========================================\n");
        System.out.println("Reading from: " + args[0]);

        processRequests(args[0]);
        displayStatistics();
    }
}
