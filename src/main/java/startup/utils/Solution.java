package startup.utils;

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int x) {
        val = x;
    }
}

public class Solution {
    //将排序的二叉树转换成平衡二叉树
    public TreeNode sortedArrayToBST(int[] num) {
        if (num == null || num.length == 0) return null;
        return ArrayToBST(num, 0, num.length - 1);
    }

    //递归调用获得平衡二叉树
    public TreeNode ArrayToBST(int[] num, int start, int end) {
        if (start > end) return null;
        int mid = start + (end - start + 1) / 2;
        TreeNode root = new TreeNode(num[mid]);
        root.left = ArrayToBST(num, start, mid - 1);
        root.right = ArrayToBST(num, mid + 1, end);
        return root;
    }

    public void inOrder(TreeNode root) {
        if (root != null) {
            inOrder(root.left);
            System.out.println(root.val);
            inOrder(root.right);
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        int[] arr = {1, 2, 3, 4, 5, 6};
        Solution s = new Solution();
        s.inOrder(s.sortedArrayToBST(arr));
    }
}
